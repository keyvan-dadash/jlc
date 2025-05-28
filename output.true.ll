declare void @printInt(i32)
declare void @printDouble(double)
declare void @printString(i8*)
declare i32 @readInt()
declare double @readDouble()

@g0 = global [5 x i8] c"true\00"
@g1 = global [6 x i8] c"false\00"
define i32 @main() {
entry:
    %var0_a = alloca i1
    store i1 0, i1* %var0_a
    %var1_b = alloca i1
    store i1 1, i1* %var1_b
    %var2_c = alloca i1
    store i1 0, i1* %var2_c
    %t0 = load i1, i1* %var0_a
    br i1 %t0, label %and.first.true_1, label %and.first.false_1
and.first.false_1:
    br label %and.end_1
and.first.true_1:
    %t1 = load i1, i1* %var1_b
    br i1 %t1, label %and.first.true_2, label %and.first.false_2
and.first.false_2:
    br label %and.end_2
and.first.true_2:
    %t2 = load i1, i1* %var2_c
    %t3 = and i1 %t1, %t2
    br label %and.end_2
and.end_2:
    %t4 = phi i1 [ %t3, %and.first.true_2 ], [ %t1, %and.first.false_2 ]
    %t5 = and i1 %t0, %t4
    br label %and.end_1
and.end_1:
    %t6 = phi i1 [ %t5, %and.end_2 ], [ %t0, %and.first.false_1 ]
    br i1 %t6, label %if.statement_0, label %else.statement_0
if.statement_0:
    %t8 = getelementptr inbounds [5 x i8], [5 x i8]* @g0, i32 0, i32 0
    call void @printString(i8* %t8)
    br label %if.end_0
else.statement_0:
    %t10 = getelementptr inbounds [6 x i8], [6 x i8]* @g1, i32 0, i32 0
    call void @printString(i8* %t10)
    br label %if.end_0
if.end_0:
    ret i32 0
}

