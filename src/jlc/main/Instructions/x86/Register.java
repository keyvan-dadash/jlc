package jlc.main.Instructions.x86;

/*
 * Represents registers in x86 arch.
 */
public enum Register {
    // 64-bit GPs
    RAX("rax"), RBX("rbx"), RCX("rcx"), RDX("rdx"),
    RSI("rsi"), RDI("rdi"), RBP("rbp"), RSP("rsp"),
    R8 ("r8" ), R9 ("r9" ), R10("r10"), R11("r11"),
    R12("r12"), R13("r13"), R14("r14"), R15("r15"),

    // RIP for realitive access
    RIP("rip"),

    // 32-bit aliases
    EAX("eax"), EBX("ebx"), ECX("ecx"), EDX("edx"),
    ESI("esi"), EDI("edi"), EBP("ebp"), ESP("esp"),
    R8D ("r8d" ), R9D ("r9d" ), R10D("r10d"), R11D("r11d"),
    R12D("r12d"), R13D("r13d"), R14D("r14d"), R15D("r15d"),

    // 16-bit aliases
    AX("ax"), BX("bx"), CX("cx"), DX("dx"),
    SI("si"), DI("di"), BP("bp"), SP("sp"),
    R8W("r8w"), R9W("r9w"), R10W("r10w"), R11W("r11w"),
    R12W("r12w"), R13W("r13w"), R14W("r14w"), R15W("r15w"),

    // 8-bit low aliases
    AL("al"), BL("bl"), CL("cl"), DL("dl"),
    SIL("sil"), DIL("dil"), BPL("bpl"), SPL("spl"),
    R8B("r8b"), R9B("r9b"), R10B("r10b"), R11B("r11b"),
    R12B("r12b"), R13B("r13b"), R14B("r14b"), R15B("r15b"),

    // 8-bit high aliases
    AH("ah"), BH("bh"), CH("ch"), DH("dh"),

    // XMM SSE regs
    XMM0("xmm0"), XMM1("xmm1"), XMM2("xmm2"), XMM3("xmm3"),
    XMM4("xmm4"), XMM5("xmm5"), XMM6("xmm6"), XMM7("xmm7");

    private final String name;
    Register(String name) { this.name = name; }
    public String getName() { return name; }
    @Override public String toString() { return name; }

    public static Register[] gpForAllocations() {
        return new Register[]{ 
            // RAX,
            RBX, RCX, RDX, RSI, RDI,
            R8,  R9,  R10, R12, R13, R14, R15 
        };
    }

    public static Register[] xmmForAllocations() {
        return new Register[]{ 
            // XMM0, 
            XMM1, XMM2, XMM3, XMM4, XMM5, XMM6 };
    }

    public static Register[] callerSave() {
        return new Register[]{ 
            RAX, RCX, RDX, RSI, RDI, R8, R9, R10, R11, XMM0, 
            XMM1, XMM2, XMM3, XMM4, XMM5 };
    }

    public static Register[] calleeSave() {
        return new Register[]{ 
            RBX, RBP, R12, R13, R14, R15, XMM6, XMM7 };
    }

    public static Register gpScratch()  { return R11;  }
    public static Register xmmScratch(){ return XMM7; }

    public Register forMemSize(MemSize size) {
        switch (size) {
            case QWORD:
                return widenTo64(this);
            case DWORD:
                return mapTo32(widenTo64(this));
            case BYTE:
                return mapTo8(widenTo64(this));
            default:
                throw new IllegalArgumentException("Unknown MemSize: " + size);
        }
    }

    private static Register widenTo64(Register r) {
        switch (r) {
            case AL:   case AX:  case EAX:  case RAX:  return RAX;
            case BL:   case BX:  case EBX:  case RBX:  return RBX;
            case CL:   case CX:  case ECX:  case RCX:  return RCX;
            case DL:   case DX:  case EDX:  case RDX:  return RDX;
            case SIL:  case SI:  case ESI:  case RSI:  return RSI;
            case DIL:  case DI:  case EDI:  case RDI:  return RDI;
            case BPL:  case BP:  case EBP:  case RBP:  return RBP;
            case SPL:  case SP:  case ESP:  case RSP:  return RSP;
            case R8B:  case R8W: case R8D:  case R8:   return R8;
            case R9B:  case R9W: case R9D:  case R9:   return R9;
            case R10B: case R10W:case R10D: case R10:  return R10;
            case R11B: case R11W:case R11D: case R11:  return R11;
            case R12B: case R12W:case R12D: case R12:  return R12;
            case R13B: case R13W:case R13D: case R13:  return R13;
            case R14B: case R14W:case R14D: case R14:  return R14;
            case R15B: case R15W:case R15D: case R15:  return R15;
            default:
                throw new IllegalArgumentException("Not a GP register: " + r);
        }
    }

    private static Register mapTo32(Register r64) {
        switch (r64) {
            case RAX:  return EAX;
            case RBX:  return EBX;
            case RCX:  return ECX;
            case RDX:  return EDX;
            case RSI:  return ESI;
            case RDI:  return EDI;
            case RBP:  return EBP;
            case RSP:  return ESP;
            case R8:   return R8D;
            case R9:   return R9D;
            case R10:  return R10D;
            case R11:  return R11D;
            case R12:  return R12D;
            case R13:  return R13D;
            case R14:  return R14D;
            case R15:  return R15D;
            default:
                throw new IllegalArgumentException("Cannot map to 32-bit: " + r64);
        }
    }

    private static Register mapTo8(Register r64) {
        switch (r64) {
            case RAX:  return AL;
            case RBX:  return BL;
            case RCX:  return CL;
            case RDX:  return DL;
            case RSI:  return SIL;
            case RDI:  return DIL;
            case RBP:  return BPL;
            case RSP:  return SPL;
            case R8:   return R8B;
            case R9:   return R9B;
            case R10:  return R10B;
            case R11:  return R11B;
            case R12:  return R12B;
            case R13:  return R13B;
            case R14:  return R14B;
            case R15:  return R15B;
            default:
                throw new IllegalArgumentException("No byte-alias for: " + r64);
        }
    }

    public int getWidth() {
        switch (this) {
            case AL: case BL: case CL: case DL:
            case SIL: case DIL: case BPL: case SPL:
            case R8B: case R9B: case R10B: case R11B:
            case R12B: case R13B: case R14B: case R15B:
                return 8;

            case AX: case BX: case CX: case DX:
            case SI: case DI: case BP: case SP:
            case R8W: case R9W: case R10W: case R11W:
            case R12W: case R13W: case R14W: case R15W:
                return 16;

            case EAX: case EBX: case ECX: case EDX:
            case ESI: case EDI: case EBP: case ESP:
            case R8D: case R9D: case R10D: case R11D:
            case R12D: case R13D: case R14D: case R15D:
                return 32;

            case RAX: case RBX: case RCX: case RDX:
            case RSI: case RDI: case RBP: case RSP:
            case R8:  case R9:  case R10: case R11:
            case R12: case R13: case R14: case R15:
            case RIP:
                return 64;

            case XMM0: case XMM1: case XMM2: case XMM3:
            case XMM4: case XMM5: case XMM6: case XMM7:
                return 128;

            default:
                throw new IllegalStateException("Unknown register: " + this);
        }
    }
}
