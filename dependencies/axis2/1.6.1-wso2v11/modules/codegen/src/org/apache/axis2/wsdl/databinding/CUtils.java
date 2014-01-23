/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.wsdl.databinding;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

public class CUtils {

    static final String keywords[] =
            {
                   
                    // c/ c++ keywords
                    "asm", "auto", "bool", "break", "break", "case", "catch", "char", "class", "const", "const_cast",
                    "continue", "default", "delete", "do", "double", "dynamic_cast", "else", "enum", "explicit",
                    "export", "extern", "false", "float", "for", "friend", "goto", "if", "inline", "int", "long",
                    "mutable", "namespace", "new", "operator", "private", "protected", "public", "register",
                    "reinterpret_cast", "return", "short", "signed", "sizeof", "static", "static_cast", "struct",
                    "switch", "template", "this", "throw", "true", "try", "typedef", "typeid", "typename", "union",
                    "unsigned", "using", "virtual", "void", "volatile", "wchar_t", "while",

                    //microsoft c++ keywords reference: http://msdn2.microsoft.com/en-US/library/2e6a4at9(VS.80).aspx
                    "__abstract", "abstract", "__alignof Operator", "array", "__asm", "__assume", "__based", "bool",
                    "__box", "break", "case", "catch", "__cdecl", "char", "class", "const", "const_cast", "continue",
                    "__declspec", "default", "__delegate", "delegate", "delete", "deprecated", "dllexport", "dllimport",
                    "do", "double", "dynamic_cast", "else", "enum", "enum class", "enum struct", "event", "__event",
                    "__except", "explicit", "extern", "false", "__fastcall", "__finally", "finally", "float", "for",
                    "for each", "in", "__forceinline", "friend", "friend_as", "__gc", "gcnew", "generic", "goto",
                    "__hook", "__identifier", "if", "__if_exists", "__if_not_exists", "initonly", "__inline", "inline",
                    "int", "__int8", "__int16", "__int32", "__int64", "__interface", "interface class",
                    "interface struct", "interior_ptr", "__leave", "literal", "long", "__m64", "__m128", "__m128d",
                    "__m128i", "__multiple_inheritance", "mutable", "naked", "namespace", "new", "new", "__nogc",
                    "noinline", "__noop", "noreturn", "nothrow", "novtable", "nullptr", "operator", "__pin", "private",
                    "__property", "property", "property", "protected", "public", "__raise", "ref struct", "ref class",
                    "register", "reinterpret_cast", "return", "safecast", "__sealed", "sealed", "selectany", "short",
                    "signed", "__single_inheritance", "sizeof", "static", "static_cast", "__stdcall", "struct",
                    "__super", "switch", "template", "this", "thread", "throw", "true", "try", "__try/__except",
                    "__try/__finally", "__try_cast", "typedef", "typeid", "typeid", "typename", "__unaligned",
                    "__unhook", "union", "unsigned", "using declaration", "using directive", "uuid", "__uuidof",
                    "value struct", "value class", "__value", "virtual", "__virtual_inheritance", "void", "volatile",
                    "__w64", "__wchar_t", "wchar_t", "while",
            };

    /** Collator for comparing the strings */
    static final Collator englishCollator = Collator.getInstance(Locale.ENGLISH);

    /** Use this character as suffix */
    static final char keywordPrefix = '_';


    /**
     * Checks if the input string is a valid C keyword.
     *
     * @return Returns boolean.
     */
    public static boolean isCKeyword(String keyword) {
        return (Arrays.binarySearch(keywords, keyword, englishCollator) >= 0);
    }

    /**
     * Turns a C keyword string into a non-C keyword string.  (Right now this simply means appending
     * an underscore.)
     */
    public static String makeNonCKeyword(String keyword) {
        return keywordPrefix + keyword ;
    }


}
