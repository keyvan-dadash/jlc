[BITS 64]

%ifidn __OUTPUT_FORMAT__, macho64
  %define label(X) _ %+ X
%elifidn __OUTPUT_FORMAT__, elf64
  %define label(X) X
%else
  %error "Format needs to be macho64 or elf64."
%endif

%define call_(X) call label(X)
%define jmp_(X)  jmp  label(X)

extern label(printf)
extern label(puts)
extern label(scanf)

section .data

ifmt1   db      "%d",   0xA, 0x0
ifmt2   db      "%d",   0x0
ffmt1   db      "%.1f", 0xA, 0x0
ffmt2   db      "%lf",  0x0

section .text

global label(printString)
label(printString):
    push rbp
    mov rbp, rsp

    sub rsp, 8                  ; align the stack to 16 bytes

    ; Load string pointer from stack into rdi
    mov rdi, [rbp + 16]     ; 1st argument to puts
    call puts

    add rsp, 8                 ; 🔧 8 to undo alignment + 8 to clean pushed arg

    pop rbp
    ret

global label(printInt)
label(printInt):
    push rbp
    mov rbp, rsp

    sub rsp, 8                  ; align the stack to 16 bytes

    mov rsi, [rbp + 16]         ; 2nd arg: integer to print
    lea rdi, [rel ifmt1]        ; 1st arg: format string
    xor eax, eax                ; no float args
    call printf

    add rsp, 8                 ; 🔧 8 to undo alignment + 8 to clean pushed arg

    pop rbp
    ret
       
global label(printDouble)
label(printDouble):
    push rbp
    mov rbp, rsp

    sub rsp, 8                  ; align the stack to 16 bytes

    ; Load the double argument from the stack into xmm0
    movsd xmm0, qword [rbp + 16]   ; 1st argument: double in xmm0
    lea    rdi, [rel ffmt1]        ; 2nd argument: format string ("%f\n")
    mov    al, 1                   ; tell printf there's 1 float argument (in xmm0)
    call   printf

    add rsp, 8                 ; 🔧 8 to undo alignment + 8 to clean pushed arg

    pop rbp
    ret

global label(readInt)
label(readInt):
    push rbp
    mov rbp, rsp
    sub rsp, 24              ; reserve aligned space for 1 int

    lea rdi, [rel ifmt2]
    lea rsi, [rbp - 8]       ; safer alignment (8-byte-aligned address)
    xor eax, eax
    call scanf

    mov eax, [rbp - 8]       ; read 32-bit int into eax (return)
    add rsp, 24
    pop rbp
    ret

global label(readDouble)
label(readDouble):
    push rbp
    mov rbp, rsp
    sub rsp, 24                ; reserve 16 bytes (aligned) for local double

    lea rdi, [rel ffmt2]       ; format string: "%lf"
    lea rsi, [rbp - 8]         ; address of local double
    xor eax, eax               ; no float args to scanf
    call scanf

    movsd xmm0, qword [rbp - 8] ; move result into xmm0 (return value)
    add rsp, 24
    pop rbp
    ret
