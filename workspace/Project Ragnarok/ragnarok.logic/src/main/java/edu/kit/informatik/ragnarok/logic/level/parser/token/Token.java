package edu.kit.informatik.ragnarok.logic.level.parser.token;

import java.util.HashMap;
import java.util.Map;

public class Token {

	/** Map for Special Identifiers to Special TokenType. */
	private static final Map<String, TokenType> SPECIAL_ID_MAP_TO_TYPES = new HashMap<String, TokenType>() {
		/**
		 * UID
		 */
		private static final long serialVersionUID = -7109095209912302200L;

		{
			this.put("{", TokenType.BEGIN);
			this.put("}", TokenType.END);

		}
	};

	/** The type of the Token */
	private TokenType type;

	/** The value of the Token */
	private String value;

	/**
	 * This creates a new Token by Value.
	 *
	 * @param value
	 *            value
	 */
	public Token(String value) {
		this.value = value;
		this.type = this.calcType(value);
	}

	/**
	 * This creates an EOS token.
	 */
	public Token() {
		this.value = null;
		this.type = TokenType.EOS;
	}

	/**
	 * Calculate type
	 *
	 * @param input
	 *            the input
	 * @return the token type
	 */
	private TokenType calcType(String input) {
		TokenType type = Token.SPECIAL_ID_MAP_TO_TYPES.get(input);
		if (type == null) {
			return this.determinateByContent(input);
		}
		return type;
	}

	private TokenType determinateByContent(String input) {
		if (input.matches("#ALIAS::(\\w|(\\+|-)?\\d|\\.)+->(\\w|\\d|\\.)+")) {
			return TokenType.ALIAS;
		}
		if (input.matches("#SETTING::(\\w|(\\+|-)?\\d|\\.)+->(\\w|\\d|\\.)+")) {
			return TokenType.SETTING;
		}
		return TokenType.RAW;
	}

	/**
	 * Get the TokenType
	 *
	 * @return the Type
	 */
	public TokenType getType() {
		return this.type;
	}

	/**
	 * Get the Token Value
	 *
	 * @return the Value
	 */
	public String getValue() {
		return this.value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{Type: " + this.type + " Value: " + this.value + "}";
	}
}