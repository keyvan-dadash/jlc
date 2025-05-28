#!/bin/bash
set -e  # Stop on error

# === CONFIG ===
ASM_FILE="output.asm"
RUNTIME_FILE="lib/runtime.s"
OUTPUT_EXE="myprogram"

echo "[+] Assembling $ASM_FILE with NASM (debug mode)..."
nasm -f elf64 -F dwarf -g -o output.o "$ASM_FILE"

echo "[+] Assembling $RUNTIME_FILE..."
nasm -f elf64 -F dwarf -g -o runtime.o "$RUNTIME_FILE"

echo "[+] Linking with debug symbols..."
clang -no-pie -g -O0 -o "$OUTPUT_EXE" output.o runtime.o

echo "[✓] Debug build complete. You can run: gdb ./$OUTPUT_EXE"

