package com.example.kaumedicare.OpenAPI.service;

import com.example.kaumedicare.Medicine.model.Medicine;
import com.example.kaumedicare.Medicine.repository.MedicineRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineDurApiService {
    private static final String DUR_BASE_URL = "https://apis.data.go.kr/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03";
    private static final int TOTAL_COUNT = 309747;
    private static final int NUM_OF_ROWS = 100;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long RATE_LIMIT_DELAY_MS = 500;
    private static final int BATCH_SIZE = 1000;
    private static final int PARALLEL_THREADS = 5;

    private ExecutorService executorService;

    @Value("${api.service-key}")
    private String serviceKey;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(PARALLEL_THREADS);
    }

    @PreDestroy
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        }
    }

    public void fetchAndSaveAllDurMedicines(MedicineRepository medicineRepository) {
        int totalPages = (int) Math.ceil((double) TOTAL_COUNT / NUM_OF_ROWS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger skipCount = new AtomicInteger(0);
        Set<String> processedItemSeqs = Collections.synchronizedSet(new HashSet<>());

        BlockingQueue<Medicine> medicineQueue = new LinkedBlockingQueue<>(BATCH_SIZE * 2);

        // 배치 저장 작업
        CompletableFuture<Void> batchSaveFuture = CompletableFuture.runAsync(() -> {
            List<Medicine> batch = new ArrayList<>(BATCH_SIZE);
            while (true) {
                try {
                    Medicine medicine = medicineQueue.poll(5, TimeUnit.SECONDS);
                    if (medicine == null && Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    if (medicine != null) {
                        batch.add(medicine);
                    }

                    if (batch.size() >= BATCH_SIZE || (medicine == null && !batch.isEmpty())) {
                        medicineRepository.saveAll(batch);
                        log.debug("배치 저장 완료: {}개", batch.size());
                        batch.clear();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (!batch.isEmpty()) {
                medicineRepository.saveAll(batch);
                log.debug("최종 배치 저장 완료: {}개", batch.size());
            }
        });

        // 페이지 병렬 처리
        int chunkSize = totalPages / PARALLEL_THREADS + 1;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < PARALLEL_THREADS; i++) {
            int startPage = i * chunkSize + 1;
            int endPage = Math.min((i + 1) * chunkSize, totalPages);

            futures.add(CompletableFuture.runAsync(() ->
                            processPageRange(startPage, endPage, medicineRepository, medicineQueue,
                                    processedItemSeqs, successCount, skipCount),
                    executorService));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            batchSaveFuture.join();

            log.info("DUR API 작업 완료: 총 {}개 저장, {}개 스킵",
                    successCount.get(), skipCount.get());
        } catch (Exception e) {
            log.error("DUR API 의약품 데이터 가져오기 실패: ", e);
            throw new RuntimeException("DUR API 의약품 데이터 가져오기 실패: " + e.getMessage());
        }
    }

    private void processPageRange(int startPage, int endPage,
                                  MedicineRepository medicineRepository,
                                  BlockingQueue<Medicine> medicineQueue,
                                  Set<String> processedItemSeqs,
                                  AtomicInteger successCount,
                                  AtomicInteger skipCount) {
        for (int pageNo = startPage; pageNo <= endPage; pageNo++) {
            try {
                log.info("DUR API 페이지 {} 처리 중 ({}/{})", pageNo, pageNo, endPage);
                final int currentPage = pageNo;
                JsonNode items = retryWithBackoff(() -> {
                    try {
                        return fetchPageData(currentPage);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                if (items == null || items.isEmpty() || items.isNull()) {
                    log.warn("DUR API 페이지 {}: 데이터가 없습니다. 다음 페이지로 진행합니다.", pageNo);
                    continue;
                }

                for (JsonNode item : items) {
                    processMedicineItem(item, "ITEM_SEQ", "ITEM_NAME", "ENTP_NAME",
                            medicineRepository, medicineQueue, processedItemSeqs,
                            successCount, skipCount);

                    processMedicineItem(item, "MIXTURE_ITEM_SEQ", "MIXTURE_ITEM_NAME", "MIXTURE_ENTP_NAME",
                            medicineRepository, medicineQueue, processedItemSeqs,
                            successCount, skipCount);
                }

                log.info("DUR API 페이지 {} 완료: {}개 저장, {}개 스킵 (총 저장: {})",
                        pageNo, successCount.get(), skipCount.get());

                Thread.sleep(RATE_LIMIT_DELAY_MS);
            } catch (Exception e) {
                log.error("페이지 {} 처리 중 오류 발생: {}. 다음 페이지로 진행합니다.", pageNo, e.getMessage());
                try {
                    // 오류 발생 시에도 rate limit은 지켜줍니다
                    Thread.sleep(RATE_LIMIT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("스레드 인터럽트 발생");
                    break;
                }
                continue; // 다음 페이지로 진행
            }
        }
    }

    private void processMedicineItem(JsonNode item,
                                     String seqField, String nameField, String entpField,
                                     MedicineRepository medicineRepository,
                                     BlockingQueue<Medicine> medicineQueue,
                                     Set<String> processedItemSeqs,
                                     AtomicInteger successCount,
                                     AtomicInteger skipCount) {
        String itemSeq = item.path(seqField).asText();
        if (itemSeq != null && !itemSeq.isEmpty()) {
            if (!processedItemSeqs.contains(itemSeq) && !medicineRepository.existsByItemSeq(itemSeq)) {
                try {
                    String itemName = item.path(nameField).asText();
                    String processedItemName = itemName != null ?
                            (itemName.length() > 1000 ? itemName.substring(0, 1000) : itemName) : "";

                    Medicine medicine = Medicine.builder()
                            .itemSeq(itemSeq)
                            .itemName(processedItemName)
                            .entpName(item.path(entpField).asText())
                            .build();

                    medicineQueue.put(medicine);
                    processedItemSeqs.add(itemSeq);
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                skipCount.incrementAndGet();
            }
        }
    }

    private JsonNode fetchPageData(int pageNo) throws Exception {
        String urlBuilder = buildUrl(pageNo);
        URL url = new URL(urlBuilder);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String response = readResponse(conn.getInputStream());
                validateJsonResponse(response);
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readTree(response).path("body").path("items");
            } else {
                String errorResponse = readResponse(conn.getErrorStream());
                log.error("DUR API 오류 응답 (페이지 {}): {}", pageNo, errorResponse);
                throw new RuntimeException("API 호출 실패: " + conn.getResponseCode());
            }
        } finally {
            conn.disconnect();
        }
    }

    private void validateJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("빈 응답이 반환되었습니다.");
        }
        if (response.trim().startsWith("<")) {
            throw new RuntimeException("API가 HTML을 반환했습니다. 유효한 JSON이 아닙니다.");
        }
    }

    private <T> T retryWithBackoff(Supplier<T> operation) throws Exception {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < MAX_RETRIES) {
                    long delay = RETRY_DELAY_MS * (long) Math.pow(2, attempts - 1);
                    log.warn("재시도 {} / {}, {}ms 후 다시 시도합니다. 오류: {}",
                            attempts, MAX_RETRIES, delay, e.getMessage());
                    Thread.sleep(delay);
                }
            }
        }
        throw new RuntimeException("최대 재시도 횟수 초과", lastException);
    }

    private String buildUrl(int pageNo) throws Exception {
        return DUR_BASE_URL + "?" +
                URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + serviceKey +
                "&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(String.valueOf(NUM_OF_ROWS), StandardCharsets.UTF_8) +
                "&" + URLEncoder.encode("type", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8);
    }

    private String readResponse(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}