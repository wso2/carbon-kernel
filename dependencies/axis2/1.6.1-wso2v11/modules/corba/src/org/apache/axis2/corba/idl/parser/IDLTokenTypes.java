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

// $ANTLR 2.7.6 (2005-12-22): "org.apache.axis2.corba.idl.g" -> "IDLParser.java"$

  package org.apache.axis2.corba.idl.parser;

public interface IDLTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int SEMI = 4;
	int LITERAL_abstract = 5;
	int LITERAL_local = 6;
	int LITERAL_interface = 7;
	int LITERAL_custom = 8;
	int LITERAL_valuetype = 9;
	int LITERAL_eventtype = 10;
	int LITERAL_module = 11;
	int LCURLY = 12;
	int RCURLY = 13;
	int PREPROC_DIRECTIVE = 14;
	int COLON = 15;
	int COMMA = 16;
	int SCOPEOP = 17;
	int IDENT = 18;
	int LITERAL_truncatable = 19;
	int LITERAL_supports = 20;
	int LITERAL_public = 21;
	int LITERAL_private = 22;
	int LITERAL_factory = 23;
	int LPAREN = 24;
	int RPAREN = 25;
	int LITERAL_in = 26;
	int LITERAL_const = 27;
	int ASSIGN = 28;
	int OR = 29;
	int XOR = 30;
	int AND = 31;
	int LSHIFT = 32;
	int RSHIFT = 33;
	int PLUS = 34;
	int MINUS = 35;
	int STAR = 36;
	int DIV = 37;
	int MOD = 38;
	int TILDE = 39;
	int LITERAL_TRUE = 40;
	int LITERAL_FALSE = 41;
	int LITERAL_typedef = 42;
	int LITERAL_native = 43;
	int LITERAL_float = 44;
	int LITERAL_double = 45;
	int LITERAL_long = 46;
	int LITERAL_short = 47;
	int LITERAL_unsigned = 48;
	int LITERAL_char = 49;
	int LITERAL_wchar = 50;
	int LITERAL_boolean = 51;
	int LITERAL_octet = 52;
	int LITERAL_any = 53;
	int LITERAL_Object = 54;
	int LITERAL_struct = 55;
	int LITERAL_union = 56;
	int LITERAL_switch = 57;
	int LITERAL_case = 58;
	int LITERAL_default = 59;
	int LITERAL_enum = 60;
	int LITERAL_sequence = 61;
	int LT = 62;
	int GT = 63;
	int LITERAL_string = 64;
	int LITERAL_wstring = 65;
	int LBRACK = 66;
	int RBRACK = 67;
	int LITERAL_exception = 68;
	int LITERAL_oneway = 69;
	int LITERAL_void = 70;
	int LITERAL_out = 71;
	int LITERAL_inout = 72;
	int LITERAL_raises = 73;
	int LITERAL_context = 74;
	int LITERAL_fixed = 75;
	int LITERAL_ValueBase = 76;
	int LITERAL_import = 77;
	int LITERAL_typeid = 78;
	int LITERAL_typeprefix = 79;
	int LITERAL_readonly = 80;
	int LITERAL_attribute = 81;
	int LITERAL_getraises = 82;
	int LITERAL_setraises = 83;
	int LITERAL_component = 84;
	int LITERAL_provides = 85;
	int LITERAL_uses = 86;
	int LITERAL_multiple = 87;
	int LITERAL_emits = 88;
	int LITERAL_publishes = 89;
	int LITERAL_consumes = 90;
	int LITERAL_home = 91;
	int LITERAL_manages = 92;
	int LITERAL_primarykey = 93;
	int LITERAL_finder = 94;
	int INT = 95;
	int OCTAL = 96;
	int HEX = 97;
	int STRING_LITERAL = 98;
	int WIDE_STRING_LITERAL = 99;
	int CHAR_LITERAL = 100;
	int WIDE_CHAR_LITERAL = 101;
	int FIXED = 102;
	int FLOAT = 103;
	int QUESTION = 104;
	int DOT = 105;
	int NOT = 106;
	int WS = 107;
	int SL_COMMENT = 108;
	int ML_COMMENT = 109;
	int ESC = 110;
	int VOCAB = 111;
	int DIGIT = 112;
	int NONZERODIGIT = 113;
	int OCTDIGIT = 114;
	int HEXDIGIT = 115;
	int ESCAPED_IDENT = 116;
}
