section .rodata
g0:
    dq 100.0
g1:
    dq 100.0
g2:
    dq 100.0
g3:
    dq 100.0
g4:
    dq 200.0
g5:
    dq 200.0
g6:
    dq 200.0
g7:
    dq 200.0
g8:
    dq 300.0
g9:
    dq 300.0
g10:
    dq 300.0
g11:
    dq 300.0
g12:
    dq 400.0
g13:
    dq 400.0
g14:
    dq 400.0
g15:
    dq 400.0
g16:
    dq 2.0
g17:
    dq 2.0
g18:
    dq 1.0
g19:
    dq 0.0
g20:
    dq 2.0
g21:
    dq 2.0
g22:
    dq 1.0
g23:
    dq 0.0
g24:
    dq 2.0
g25:
    dq 2.0
g26:
    dq 1.0
g27:
    dq 0.0
g28:
    dq 2.0
g29:
    dq 2.0
g30:
    dq 1.0
g31:
    dq 0.0
section .text
extern printString
extern printInt
extern printDouble
extern readInt
extern readDouble
global main
main:
    push rbp
    mov rbp, rsp
    sub rsp, 96
    movsd xmm2, QWORD [rel g0]
    movsd xmm3, QWORD [rel g1]
    movsd xmm6, QWORD [rel g2]
    movsd xmm4, QWORD [rel g3]
    movsd xmm5, QWORD [rel g4]
    movsd xmm7, QWORD [rel g5]
    movsd QWORD [rbp-8], xmm7
    movsd xmm7, QWORD [rel g6]
    movsd QWORD [rbp-16], xmm7
    movsd xmm1, QWORD [rel g7]
    movsd QWORD [rbp-24], xmm1
    movsd xmm7, QWORD [rel g8]
    movsd QWORD [rbp-32], xmm7
    movsd xmm7, QWORD [rel g9]
    movsd QWORD [rbp-40], xmm7
    movsd xmm7, QWORD [rel g10]
    movsd QWORD [rbp-48], xmm7
    movsd xmm7, QWORD [rel g11]
    movsd QWORD [rbp-56], xmm7
    movsd xmm7, QWORD [rel g12]
    movsd QWORD [rbp-64], xmm7
    movsd xmm7, QWORD [rel g13]
    movsd QWORD [rbp-72], xmm7
    movsd xmm1, QWORD [rel g14]
    movsd QWORD [rbp-80], xmm1
    movsd xmm7, QWORD [rel g15]
    movsd QWORD [rbp-88], xmm7
    sub rsp, 0
    push 15
    push 14
    push 13
    push 12
    push 11
    push 10
    push 9
    push 8
    push 7
    push 6
    push 5
    sub rsp, 8
    movsd xmm7, QWORD [rbp-88]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-80]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-72]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-64]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-56]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-48]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-40]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-32]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-24]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-16]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-8]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd QWORD [rsp], xmm5
    sub rsp, 8
    movsd QWORD [rsp], xmm4
    sub rsp, 8
    movsd QWORD [rsp], xmm6
    sub rsp, 8
    movsd QWORD [rsp], xmm3
    sub rsp, 8
    movsd QWORD [rsp], xmm2
    push 4
    push 3
    push 2
    push 1
    call many_params
    add rsp, 248
    mov rbx, rax
    mov rax, 0
    add rsp, 96
    pop rbp
    ret
many_params:
    push rbp
    mov rbp, rsp
    sub rsp, 128
    ; saving registers
    push rbx
    push r12
    push r13
    push r14
    push r15
    sub rsp, 8
    movsd QWORD [rsp], xmm6
    sub rsp, 8
    movsd QWORD [rsp], xmm7
    ; end of saving registers
    movsxd rcx, DWORD QWORD [rbp+16]
    sub rsp, 0
    push rcx
    call printInt
    add rsp, 8
    mov rdx, rax
    movsxd rsi, DWORD QWORD [rbp+176]
    sub rsp, 0
    push rsi
    call printInt
    add rsp, 8
    mov rdi, rax
    movsxd r8, DWORD QWORD [rbp+208]
    sub rsp, 0
    push r8
    call printInt
    add rsp, 8
    mov r9, rax
    movsxd r10, DWORD QWORD [rbp+240]
    sub rsp, 0
    push r10
    call printInt
    add rsp, 8
    mov r12, rax
    movsd xmm2, QWORD QWORD [rbp+48]
    sub rsp, 0
    sub rsp, 8
    movsd QWORD [rsp], xmm2
    call printDouble
    add rsp, 8
    mov r13, rax
    movsd xmm3, QWORD QWORD [rbp+80]
    sub rsp, 0
    sub rsp, 8
    movsd QWORD [rsp], xmm3
    call printDouble
    add rsp, 8
    mov r14, rax
    movsd xmm6, QWORD QWORD [rbp+112]
    sub rsp, 0
    sub rsp, 8
    movsd QWORD [rsp], xmm6
    call printDouble
    add rsp, 8
    mov r15, rax
    movsd xmm4, QWORD QWORD [rbp+144]
    sub rsp, 0
    sub rsp, 8
    movsd QWORD [rsp], xmm4
    call printDouble
    add rsp, 8
    mov rbx, rax
    movsxd rcx, DWORD QWORD [rbp+16]
    cmp rcx, 2
    setne al
    movzx rax, al
    test rax, rax
    je if.end_0
    movsxd rdx, DWORD QWORD [rbp+256]
    movsxd rsi, DWORD QWORD [rbp+16]
    movsxd rdi, DWORD QWORD [rbp+24]
    movsxd r8, DWORD QWORD [rbp+32]
    movsd xmm5, QWORD QWORD [rbp+72]
    movsd xmm1, QWORD [rel g16]
    movsd xmm3, xmm5
    divsd xmm3, xmm1
    movsd xmm6, QWORD QWORD [rbp+48]
    movsd xmm2, QWORD [rel g17]
    movsd xmm5, xmm6
    mulsd xmm5, xmm2
    movsd xmm1, QWORD QWORD [rbp+56]
    movsd xmm4, QWORD [rel g18]
    movsd xmm2, xmm1
    addsd xmm2, xmm4
    movsd xmm6, QWORD QWORD [rbp+64]
    movsd xmm4, QWORD [rel g19]
    movsd xmm1, xmm6
    subsd xmm1, xmm4
    movsd xmm6, QWORD QWORD [rbp+104]
    movsd xmm4, QWORD [rel g20]
    movsd xmm6, xmm6
    divsd xmm6, xmm4
    movsd xmm4, QWORD QWORD [rbp+80]
    movsd QWORD [rbp-16], xmm6
    movsd QWORD [rbp-8], xmm1
    movsd xmm6, QWORD [rel g21]
    movsd xmm4, xmm4
    mulsd xmm4, xmm6
    movsd xmm6, QWORD QWORD [rbp+88]
    movsd QWORD [rbp-24], xmm4
    movsd xmm4, QWORD [rel g22]
    addsd xmm6, xmm4
    movsd xmm4, QWORD QWORD [rbp+96]
    movsd QWORD [rbp-32], xmm6
    movsd xmm6, QWORD [rel g23]
    subsd xmm4, xmm6
    movsd xmm6, QWORD QWORD [rbp+136]
    movsd QWORD [rbp-40], xmm4
    movsd xmm4, QWORD [rel g24]
    movsd xmm6, xmm6
    divsd xmm6, xmm4
    movsd xmm4, QWORD QWORD [rbp+112]
    movsd QWORD [rbp-48], xmm6
    movsd xmm6, QWORD [rel g25]
    movsd xmm4, xmm4
    mulsd xmm4, xmm6
    movsd xmm6, QWORD QWORD [rbp+120]
    movsd QWORD [rbp-56], xmm4
    movsd xmm4, QWORD [rel g26]
    addsd xmm6, xmm4
    movsd xmm4, QWORD QWORD [rbp+128]
    movsd QWORD [rbp-64], xmm6
    movsd xmm6, QWORD [rel g27]
    subsd xmm4, xmm6
    movsd xmm6, QWORD QWORD [rbp+168]
    movsd QWORD [rbp-72], xmm4
    movsd xmm4, QWORD [rel g28]
    movsd xmm6, xmm6
    divsd xmm6, xmm4
    movsd xmm4, QWORD QWORD [rbp+144]
    movsd QWORD [rbp-80], xmm6
    movsd xmm6, QWORD [rel g29]
    movsd xmm4, xmm4
    mulsd xmm4, xmm6
    movsd xmm6, QWORD QWORD [rbp+152]
    movsd QWORD [rbp-88], xmm4
    movsd xmm4, QWORD [rel g30]
    addsd xmm6, xmm4
    movsd xmm4, QWORD QWORD [rbp+160]
    movsd QWORD [rbp-96], xmm6
    movsd xmm6, QWORD [rel g31]
    subsd xmm4, xmm6
    movsxd r9, DWORD QWORD [rbp+40]
    movsxd r10, DWORD QWORD [rbp+176]
    movsxd r12, DWORD QWORD [rbp+184]
    movsxd r13, DWORD QWORD [rbp+192]
    movsxd r14, DWORD QWORD [rbp+200]
    movsxd r15, DWORD QWORD [rbp+208]
    movsxd rbx, DWORD QWORD [rbp+216]
    movsxd rcx, DWORD QWORD [rbp+224]
    movsxd rax, DWORD QWORD [rbp+232]
    movsxd r11, DWORD QWORD [rbp+240]
    mov QWORD [rbp-104], r11
    movsxd r11, DWORD QWORD [rbp+248]
    mov QWORD [rbp-112], r11
    sub rsp, 0
    mov r11, QWORD [rbp-112]
    push r11
    mov r11, QWORD [rbp-104]
    push r11
    push rax
    push rcx
    push rbx
    push r15
    push r14
    push r13
    push r12
    push r10
    push r9
    sub rsp, 8
    movsd QWORD [rsp], xmm4
    sub rsp, 8
    movsd xmm7, QWORD [rbp-96]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-88]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-80]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-72]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-64]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-56]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-48]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-40]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-32]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-24]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-16]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd xmm7, QWORD [rbp-8]
    movsd QWORD [rsp], xmm7
    sub rsp, 8
    movsd QWORD [rsp], xmm2
    sub rsp, 8
    movsd QWORD [rsp], xmm5
    sub rsp, 8
    movsd QWORD [rsp], xmm3
    push r8
    push rdi
    push rsi
    push rdx
    call many_params
    add rsp, 248
    mov rdx, rax
if.end_0:
    ; restoring registers
    movsd xmm7, QWORD [rsp]
    add rsp, 8
    movsd xmm6, QWORD [rsp]
    add rsp, 8
    pop r15
    pop r14
    pop r13
    pop r12
    pop rbx
    ; end of restoring registers
    add rsp, 128
    pop rbp
    ret
