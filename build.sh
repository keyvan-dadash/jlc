#!/bin/bash
# Exit immediately if a command exits with a non-zero status.
set -e

# Step 1: Convert LLVM IR files to bitcode.
echo "Converting output.ll and lib/runtime.ll to bitcode..."
llvm-as output.ll -o output.bc
llvm-as lib/runtime.ll -o runtime.bc

# Step 2: Link the bitcode files so that functions defined in runtime.bc (e.g., @printInt) are available.
echo "Linking output.bc with runtime.bc..."
llvm-link output.bc runtime.bc -o linked.bc

# Step 3: Optimize the linked bitcode.
echo "Optimizing linked bitcode..."
opt -O3 linked.bc -o optimized.bc

# Step 4: Compile the optimized bitcode into an object file.
echo "Compiling to object file..."
llc -filetype=obj optimized.bc -o final.o

# Step 5: Link the object file with gcc to produce an executable.
echo "Linking object file with gcc..."
gcc final.o -no-pie -o final_executable

echo "Build succeeded. Executable is final_executable."

# Cleanup: Remove all intermediate files except output.ll.
echo "Cleaning up intermediate files..."
rm -f output.bc runtime.bc linked.bc optimized.bc final.o

echo "Cleanup complete."

