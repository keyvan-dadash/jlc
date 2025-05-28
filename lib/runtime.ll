@dnl = internal constant [4 x i8] c"%d\0A\00"
@fnl = internal constant [6 x i8] c"%.1f\0A\00"
@d   = internal constant [3 x i8] c"%d\00"
@lf  = internal constant [4 x i8] c"%lf\00"

declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)
declare i32 @puts(i8*)
declare i8* @malloc(i64)

; =====================
; Printing Functions
; =====================
define void @printInt(i32 %x) {
entry:
  %t0 = getelementptr [4 x i8], [4 x i8]* @dnl, i32 0, i32 0
  call i32 (i8*, ...) @printf(i8* %t0, i32 %x)
  ret void
}

define void @printDouble(double %x) {
entry:
  %t0 = getelementptr [6 x i8], [6 x i8]* @fnl, i32 0, i32 0
  call i32 (i8*, ...) @printf(i8* %t0, double %x)
  ret void
}

define void @printString(i8* %s) {
entry:
  call i32 @puts(i8* %s)
  ret void
}

; =====================
; Input Functions
; =====================
define i32 @readInt() {
entry:
  %res = alloca i32
  %t1 = getelementptr [3 x i8], [3 x i8]* @d, i32 0, i32 0
  call i32 (i8*, ...) @scanf(i8* %t1, i32* %res)
  %t2 = load i32, i32* %res
  ret i32 %t2
}

define double @readDouble() {
entry:
  %res = alloca double
  %t1 = getelementptr [4 x i8], [4 x i8]* @lf, i32 0, i32 0
  call i32 (i8*, ...) @scanf(i8* %t1, double* %res)
  %t2 = load double, double* %res
  ret double %t2
}

; Allocate int array: i32[]
define i8* @alloc_array_i32(i32 %len) {
entry:
  %t0 = add i32 %len, 1
  %t1 = zext i32 %t0 to i64
  %t2 = mul i64 %t1, 4
  %t3 = call i8* @malloc(i64 %t2)
  %t4 = bitcast i8* %t3 to i32*
  store i32 %len, i32* %t4
  %t5 = getelementptr inbounds i32, i32* %t4, i32 1
  %t6 = bitcast i32* %t5 to i8*
  ret i8* %t6
}

; Allocate byte array: i8[] (char/bool)
define i8* @alloc_array_i8(i32 %len) {
entry:
  %len64 = zext i32 %len to i64
  %total_data = add i64 %len64, 4
  %mem = call i8* @malloc(i64 %total_data)
  %header_ptr = bitcast i8* %mem to i32*
  store i32 %len, i32* %header_ptr
  %data_ptr = getelementptr i8, i8* %mem, i64 4
  ret i8* %data_ptr
}

; Allocate double array: double[]
define i8* @alloc_array_double(i32 %len) {
entry:
  %len_plus_1 = add i32 %len, 1
  %len_plus_1_64 = zext i32 %len_plus_1 to i64
  %total_bytes = mul i64 %len_plus_1_64, 8
  %mem = call i8* @malloc(i64 %total_bytes)
  %header_ptr = bitcast i8* %mem to i32*
  store i32 %len, i32* %header_ptr
  %double_ptr = bitcast i8* %mem to double*
  %data_ptr = getelementptr double, double* %double_ptr, i32 1
  %ret_ptr = bitcast double* %data_ptr to i8*
  ret i8* %ret_ptr
}

define i32 @array_length(i8* %data) {
entry:
  ; Assumes the 4 bytes before data hold the length
  %t0 = getelementptr i8, i8* %data, i64 -4
  %t1 = bitcast i8* %t0 to i32*
  %t2 = load i32, i32* %t1
  ret i32 %t2
}
