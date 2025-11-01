# jlc — Javalette compiler (Array‑1 + x86‑64 backend with register allocation)

A compact, working compiler for the **Javalette** language with:

* A complete **frontend** (lexer, parser, AST, type checking)
* Two codegen paths:

  * **LLVM IR** emission
  * **Native x86‑64** emission with **register allocation** (and a small runtime)
* **Array‑1** extension (one‑dimensional arrays)

This README is a **project description**—see `doc/doc.md` for course‑specific write‑up/details.

---

## Features

* Frontend: JLex + JavaCUP pipeline, typed AST
* LLVM IR backend (linkable with a provided runtime)
* x86‑64 backend (NASM syntax), System V AMD64 ABI
* Register allocation with spilling when registers run out
* Array‑1: heap arrays that carry their length; indexing and assignment for primitive element types

*Not included*: N‑D arrays, OOP/dispatch, higher‑order functions, GC, advanced optimizations.

---

## Dependencies

* **Java (JDK 8+)**
* **JLex** and **JavaCUP**
* **make**
* **NASM** and **GCC/Clang** (for native x86‑64 builds)
* **Clang/LLVM** (for the LLVM IR path)
* POSIX‑like environment (Linux/macOS)

> After `make`, compiled classes are placed under `bin/`, and `jlc.main.Main` is on the classpath.

---

## Get the code

```sh
git clone https://github.com/keyvan-dadash/jlc.git
cd jlc
```

---

## Build the compiler

```sh
make        # builds the compiler into ./bin
```

This compiles the front‑end and both backends. The main entry point is `jlc.main.Main`.

---

## Usage

You can generate **either** LLVM IR or native **x86‑64** assembly, then use the helper scripts to produce an executable.

### A) Native x86‑64 path

1. Generate assembly from a Javalette source file:

```sh
java jlc.main.Main --x86 < src/test.jl   # writes NASM assembly to stdout
```

2. Build and link with the native runtime:

```sh
./x86build.sh
```

3. Run the resulting binary:

```sh
./myprogram
```

> The `x86build.sh` script assembles and links the generated `.asm` together with `lib/runtime.s`.

### B) LLVM IR path

1. Generate LLVM IR from a Javalette source file:

```sh
# With explicit flag\ njava jlc.main.Main --llvm < src/test.jl  # writes LLVM IR to stdout
# (The LLVM path may also be the default when no flag is given.)
```

2. Build and link with the LLVM runtime:

```sh
./build.sh
```

3. Run the resulting binary:

```sh
./final_executable
```

> The `build.sh` script links your generated IR with `lib/runtime.ll` using `clang`/LLVM tools.

---

## Runtimes & calling convention

* **LLVM**: `lib/runtime.ll` provides the required I/O primitives: `printString`, `printInt`, `printDouble`, `readInt`, `readDouble`.
* **Native**: `lib/runtime.s` implements the same primitives in NASM, targeting the **System V AMD64 ABI** for Linux/macOS.

---

## Notes on Array‑1

* Arrays are heap‑allocated and store their **length** alongside elements.
* Indexing/assignment over primitive element types are supported.
* Runtime bounds checking is not currently included.

---

## Register allocation (x86‑64)

* Conforms to the System V AMD64 calling convention.
* Uses a straightforward allocator with spilling; preserves callee/caller‑saved registers to interoperate with the runtime (`printf`/`scanf`/`puts`).

---

## Repository layout

```
.
├── doc/            # additional notes / course write-up (see doc.md)
├── lib/
│   ├── runtime.ll  # runtime for LLVM path
│   └── runtime.s   # runtime for native x86‑64 path
├── src/            # compiler sources (frontend + codegen)
├── Makefile        # build the compiler
├── build.sh        # link LLVM IR into an executable
├── x86build.sh     # assemble+link native x86‑64 executable
└── README.md
```
