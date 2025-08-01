#!/bin/bash

#
# Copyright 2023-2025 RWPP contributors
# 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
# Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
# https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
#

# 获取脚本所在目录
DestPath="$(dirname "$0")"
DestExt="*.jar*"

count=0
lastName=""

# 查找匹配的文件
while IFS= read -r file; do
    filename=$(basename "$file")

    prefix="${filename:0:18}"

    if [[ "$prefix" == "RWPP-multiplatform" ]]; then
        count=$((count + 1))
        lastName="$file"
    fi
done < <(find "$DestPath" -type f -name "$DestExt")

if [[ "$count" -eq 0 ]]; then
    echo "No RWPP-multiplatform file exists"
    read -p "Press Enter to continue..."
    exit 1
elif [[ "$count" -gt 1 ]]; then
    echo "Multiple RWPP-multiplatform file exists"
    read -p "Press Enter to continue..."
    exit 1
elif [[ "$count" -eq 1 ]]; then
    java -D"java.net.preferIPv4Stack=true" -Xmx2000M -D"file.encoding=UTF-8" -D"prism.allowhidpi=false" -D"java.library.path=." -cp "${lastName}:generated_lib/*:libs/*" io.github.rwpp.desktop.MainKt
fi

read -p "Press Enter to continue..."