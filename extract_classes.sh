#!/bin/bash

# 정확한 Diary 디렉토리 경로
diary_dir="./src/main/java/com/example/kaumedicare/Diary"

# 출력 파일 초기화
output_file="out.txt"
> "$output_file"

# Diary 디렉토리 내 모든 .java 파일 순회
find "$diary_dir" -type f -name "*.java" | while read -r file; do
  echo "Processing $file"
  echo "-------------------" >> "$output_file"
  echo "File: $file" >> "$output_file"
  echo "-------------------" >> "$output_file"
  cat "$file" >> "$output_file"
  echo -e "\n-------------------\n" >> "$output_file"
done

echo "All classes have been extracted to $output_file"
