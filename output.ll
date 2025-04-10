declare void @printInt(i32)
declare void @printDouble(double)
declare void @printString(i8*)
declare i32 @readInt()
declare double @readDouble()

define i32 @main() {
entry:
    %input = alloca i32
    %t0 = call i32 @readInt()
    store i32 %t0, i32* %input
    %t1 = load i32, i32* %input
    %t2 = call i32 @fact(i32 %t1)
    call void @printInt(i32 %t2)
    %t4 = call i32 @fact(i32 10)
    call void @printInt(i32 %t4)
    %t6 = call i32 @factr(i32 7)
    call void @printInt(i32 %t6)
    ret i32 0
    unreachable
}

define i32 @fact(i32 %__p__n) {
entry:
    %n = alloca i32
    store i32 %__p__n, i32* %n
    %i = alloca i32
    %r = alloca i32
    store i32 1, i32* %i
    store i32 1, i32* %r
    br label %while.cond_0
while.cond_0:
    %t0 = load i32, i32* %i
    %t1 = icmp sle i32 %t0, %__p__n
    br i1 %t1, label %while.st_1, label %while.end_2
while.st_1:
    %t2 = load i32, i32* %r
    %t3 = mul i32 %t2, %t0
    store i32 %t3, i32* %r
    %t4 = add i32 %t0, 1
    store i32 %t4, i32* %i
    br label %while.cond_0
while.end_2:
    %t5 = load i32, i32* %r
    ret i32 %t5
    unreachable
}

define i32 @factr(i32 %__p__n) {
entry:
    %n = alloca i32
    store i32 %__p__n, i32* %n
    %t0 = icmp slt i32 %__p__n, 2
    br i1 %t0, label %if.statement_0, label %else.statement_1
if.statement_0:
    ret i32 1
    br label %if.end_2
else.statement_1:
    %t1 = sub i32 %__p__n, 1
    %t2 = call i32 @factr(i32 %t1)
    %t3 = mul i32 %__p__n, %t2
    ret i32 %t3
    br label %if.end_2
if.end_2:
    unreachable
}

