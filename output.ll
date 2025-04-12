declare void @printInt(i32)
declare void @printDouble(double)
declare void @printString(i8*)
declare i32 @readInt()
declare double @readDouble()

@g0 = global [3 x i8] c"&&\00"
@g1 = global [3 x i8] c"||\00"
@g2 = global [2 x i8] c"!\00"
@g3 = global [6 x i8] c"false\00"
@g4 = global [5 x i8] c"true\00"
define i32 @main() {
entry:
    %t1 = getelementptr inbounds [3 x i8], [3 x i8]* @g0, i32 0, i32 0
    call void @printString(i8* %t1)
    %t2 = sub i32 0, 1
    %t3 = call i1 @test(i32 %t2)
    %t4 = call i1 @test(i32 0)
    %t5 = and i1 %t3, %t4
    call void @printBool(i1 %t5)
    %t7 = sub i32 0, 2
    %t8 = call i1 @test(i32 %t7)
    %t9 = call i1 @test(i32 1)
    %t10 = and i1 %t8, %t9
    call void @printBool(i1 %t10)
    %t12 = call i1 @test(i32 3)
    %t13 = sub i32 0, 5
    %t14 = call i1 @test(i32 %t13)
    %t15 = and i1 %t12, %t14
    call void @printBool(i1 %t15)
    %t17 = call i1 @test(i32 234234)
    %t18 = call i1 @test(i32 21321)
    %t19 = and i1 %t17, %t18
    call void @printBool(i1 %t19)
    %t22 = getelementptr inbounds [3 x i8], [3 x i8]* @g1, i32 0, i32 0
    call void @printString(i8* %t22)
    %t23 = sub i32 0, 1
    %t24 = call i1 @test(i32 %t23)
    %t25 = call i1 @test(i32 0)
    %t26 = or i1 %t24, %t25
    call void @printBool(i1 %t26)
    %t28 = sub i32 0, 2
    %t29 = call i1 @test(i32 %t28)
    %t30 = call i1 @test(i32 1)
    %t31 = or i1 %t29, %t30
    call void @printBool(i1 %t31)
    %t33 = call i1 @test(i32 3)
    %t34 = sub i32 0, 5
    %t35 = call i1 @test(i32 %t34)
    %t36 = or i1 %t33, %t35
    call void @printBool(i1 %t36)
    %t38 = call i1 @test(i32 234234)
    %t39 = call i1 @test(i32 21321)
    %t40 = or i1 %t38, %t39
    call void @printBool(i1 %t40)
    %t43 = getelementptr inbounds [2 x i8], [2 x i8]* @g2, i32 0, i32 0
    call void @printString(i8* %t43)
    call void @printBool(i1 1)
    call void @printBool(i1 0)
    ret i32 0
}

define void @printBool(i1 %__p__b) {
entry:
    %var0_b = alloca i1
    store i1 %__p__b, i1* %var0_b
    %t0 = load i1, i1* %var0_b
    %t1 = xor i1 %t0, true
    br i1 %t1, label %if.statement_0, label %else.statement_1
if.statement_0:
    %t3 = getelementptr inbounds [6 x i8], [6 x i8]* @g3, i32 0, i32 0
    call void @printString(i8* %t3)
    br label %if.end_2
else.statement_1:
    %t5 = getelementptr inbounds [5 x i8], [5 x i8]* @g4, i32 0, i32 0
    call void @printString(i8* %t5)
    br label %if.end_2
if.end_2:
    ret void
}

define i1 @test(i32 %__p__i) {
entry:
    %var0_i = alloca i32
    store i32 %__p__i, i32* %var0_i
    %t0 = load i32, i32* %var0_i
    call void @printInt(i32 %t0)
    %t2 = load i32, i32* %var0_i
    %t3 = icmp sgt i32 %t2, 0
    ret i1 %t3
}

