#!/bin/bash

# 재귀적으로 .java와 .properties 파일을 찾고 내용을 출력하는 함수
find_and_print_files() {
    local dir="$1"
    local base_dir="$2"

    shopt -s nullglob  # 매칭되는 파일이 없을 때 패턴을 그대로 반환하지 않고 빈 목록을 반환

    for file in "$dir"/*.java "$dir"/*.properties; do
        if [ -f "$file" ]; then
            # 상대 경로 계산
            relative_path="${file#$base_dir/}"

            # 파일명 출력
            echo "<$relative_path>"

            # 파일 내용 출력
            echo '```'
            cat "$file"
            echo '```'

            # 빈 줄 추가
            echo ""
        fi
    done

    # 하위 디렉터리 처리
    for subdir in "$dir"/*/ ; do
        if [ -d "$subdir" ]; then
            find_and_print_files "$subdir" "$base_dir"
        fi
    done

    shopt -u nullglob  # nullglob 옵션 해제
}

# 스크립트 실행
base_dir="$(pwd)"
find_and_print_files "$base_dir" "$base_dir"
