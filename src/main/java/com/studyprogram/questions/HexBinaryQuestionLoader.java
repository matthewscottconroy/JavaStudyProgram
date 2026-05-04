package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class HexBinaryQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.HEX_BINARY_ENCODING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("hb-mc-01", Topic.HEX_BINARY_ENCODING, 1,
                "What does the prefix '0x' mean in Java?",
                "The number is octal (base 8)",
                "The number is hexadecimal (base 16)",
                "The number is binary (base 2)",
                "The number is floating-point",
                "b",
                "0x indicates a hexadecimal literal. 0xFF = 255 in decimal. "
                + "0b prefix indicates binary (0b1010 = 10). "
                + "0 prefix alone indicates octal (017 = 15)."),

            mc("hb-mc-02", Topic.HEX_BINARY_ENCODING, 1,
                "What is the decimal value of 0xFF?",
                "15", "16", "255", "256",
                "c",
                "F in hex = 15. 0xFF = (15 × 16) + 15 = 240 + 15 = 255. "
                + "This is commonly used as a bitmask to extract the lowest 8 bits."),

            mc("hb-mc-03", Topic.HEX_BINARY_ENCODING, 2,
                "What is the binary representation of the decimal number 13?",
                "1101", "1011", "1110", "1100",
                "a",
                "13 = 8 + 4 + 1 = 2³ + 2² + 2⁰ = 1101 in binary. "
                + "1011 = 11, 1110 = 14, 1100 = 12."),

            mc("hb-mc-04", Topic.HEX_BINARY_ENCODING, 2,
                "What ASCII value (decimal) corresponds to the character 'A'?",
                "41", "65", "97", "48",
                "b",
                "'A' is ASCII 65. 'a' is 97 (lowercase letters are 32 higher). '0' is 48. "
                + "In Java: (int)'A' == 65 and (char)65 == 'A'."),

            trace("hb-tr-01", Topic.HEX_BINARY_ENCODING, 2,
                "What is printed?",
                "int x = 0b1010;\n"
                + "int y = 0xFF;\n"
                + "System.out.println(x);\n"
                + "System.out.println(y);\n"
                + "System.out.println((int)'A');",
                "10\n255\n65",
                "0b1010 = 10 (binary). 0xFF = 255 (hex). (int)'A' is the ASCII value 65."),

            debug("hb-db-01", Topic.HEX_BINARY_ENCODING, 3,
                "This code should extract the lower 8 bits but gives a wrong result. Why?",
                "int value = -1;\n"
                + "int lower8 = value & 0xFF;\n"
                + "// Expected: 255, but developer expected -1",
                "The & operator is not valid for negative numbers",
                "0xFF is the wrong mask — use 0xF instead",
                "The result is 255 (correct) — the developer misunderstood; & 0xFF always gives 0–255",
                "int cannot hold negative values",
                "c",
                "-1 in two's complement is all 1s (0xFFFFFFFF). "
                + "Masking with 0xFF keeps only the lowest 8 bits, giving 0xFF = 255. "
                + "This is correct behaviour — & 0xFF is the standard way to convert a byte to unsigned 0-255."),

            codegen("hb-cg-01", Topic.HEX_BINARY_ENCODING, 2,
                "Which expression converts the char 'Z' to its ASCII integer value?",
                "ASCII.value('Z')", "(int) 'Z'", "Character.toInt('Z')", "'Z'.toAscii()",
                "b",
                "Casting a char to int returns its Unicode/ASCII code point. "
                + "(int)'Z' == 90. Character.toInt() does not exist. "
                + "The other options are fictional API."),

            mc("hb-mc-05", Topic.HEX_BINARY_ENCODING, 2,
                "What is the decimal value of the hexadecimal number 0x1F?",
                "15", "16", "31", "256",
                "c",
                "0x1F = 1*16 + 15 = 31. "
                + "Hex digits: 0-9 are 0-9; A-F are 10-15. "
                + "F = 15. 1*16 + F(15) = 31."),

            trace("hb-tr-02", Topic.HEX_BINARY_ENCODING, 2,
                "What is printed?",
                "System.out.println(Integer.toBinaryString(12));\n"
                + "System.out.println(Integer.toHexString(255));",
                "1100\nff",
                "12 in binary: 8+4 = 1100. "
                + "255 in hex: 0xFF = ff (lowercase by default)."),

            mc("hb-mc-06", Topic.HEX_BINARY_ENCODING, 3,
                "What does Integer.parseInt(\"1A\", 16) return?",
                "1", "10", "26", "Throws NumberFormatException",
                "c",
                "parseInt(String, radix) parses in the given base. "
                + "Radix 16: 1A = 1*16 + 10 = 26. "
                + "Similarly: parseInt(\"1010\", 2) = 10 (binary), parseInt(\"17\", 8) = 15 (octal)."),

            mc("hb-mc-07", Topic.HEX_BINARY_ENCODING, 2,
                "What bitwise operator performs AND on two integers?",
                "|", "&", "^", "~",
                "b",
                "& (bitwise AND): 1 & 1 = 1; 1 & 0 = 0. "
                + "| (OR): 1 | 0 = 1. "
                + "^ (XOR): 1 ^ 1 = 0; 1 ^ 0 = 1. "
                + "~ (NOT/complement): ~0 = -1 (flips all bits)."),

            mc("hb-mc-08", Topic.HEX_BINARY_ENCODING, 2,
                "What is 5 | 3 in decimal?",
                "2", "7", "15", "8",
                "b",
                "5 = 0101, 3 = 0011. OR: 0101 | 0011 = 0111 = 7. "
                + "Each bit is 1 if either input bit is 1."),

            mc("hb-mc-09", Topic.HEX_BINARY_ENCODING, 3,
                "What does the left-shift operator << do?",
                "Moves bits to the right, filling with 0",
                "Moves bits to the left, filling with 0 on the right — equivalent to multiplying by 2^n",
                "Rotates bits circularly",
                "Performs signed division by 2",
                "b",
                "x << n shifts all bits of x left by n positions. "
                + "1 << 3 = 8 (1 shifted left 3 = 2³). "
                + "Useful for fast powers-of-2 multiplication and bit flags."),

            mc("hb-mc-10", Topic.HEX_BINARY_ENCODING, 3,
                "What does Integer.toHexString(255) return?",
                "\"0xFF\"", "\"255\"", "\"ff\"", "\"FF\"",
                "c",
                "Integer.toHexString() returns a lowercase hex string without the '0x' prefix. "
                + "255 = 0xFF → \"ff\". "
                + "For uppercase: Integer.toHexString(255).toUpperCase() → \"FF\"."),

            trace("hb-tr-03", Topic.HEX_BINARY_ENCODING, 2,
                "What is printed?",
                "int a = 0b1100;  // 12\n"
                + "int b = 0b1010;  // 10\n"
                + "System.out.println(a & b);\n"
                + "System.out.println(a | b);\n"
                + "System.out.println(a ^ b);",
                "8\n14\n6",
                "1100 & 1010 = 1000 = 8. 1100 | 1010 = 1110 = 14. 1100 ^ 1010 = 0110 = 6."),

            trace("hb-tr-04", Topic.HEX_BINARY_ENCODING, 3,
                "What is printed?",
                "int flags = 0;\n"
                + "flags |= 1;  // set bit 0\n"
                + "flags |= 4;  // set bit 2\n"
                + "System.out.println(Integer.toBinaryString(flags));",
                "101",
                "flags starts at 0. |= 1 sets bit 0: 001. |= 4 sets bit 2: 101. "
                + "toBinaryString(5) = \"101\" (no leading zeros)."),

            debug("hb-db-02", Topic.HEX_BINARY_ENCODING, 3,
                "This function to check if a bit is set gives wrong results. Why?",
                "static boolean isBitSet(int value, int bit) {\n"
                + "    return (value && (1 << bit)) != 0;\n"
                + "}",
                "1 << bit is invalid for large bit positions",
                "Java uses && for logical AND, not bitwise AND — use & instead",
                "The return type should be int, not boolean",
                "1 << bit overflows for bit >= 32",
                "b",
                "&& is the logical AND operator for boolean expressions — it cannot be used on ints. "
                + "Use the bitwise & for integer bit manipulation: return (value & (1 << bit)) != 0;"),

            codegen("hb-cg-02", Topic.HEX_BINARY_ENCODING, 3,
                "Which code checks if the 3rd bit (bit index 2) of variable 'x' is set?",
                "if (x & 2) { }",
                "if ((x & (1 << 2)) != 0) { }",
                "if (x.bit(2)) { }",
                "if (x | (1 << 2)) { }",
                "b",
                "1 << 2 = 4 (bit 2 set). x & 4 is nonzero only if bit 2 is set. "
                + "Option A checks bit 1 (1 << 1 = 2), not bit 2. "
                + "Option C: int has no bit() method. "
                + "Option D: | always sets the bit rather than checking it.")
        );
    }
}
