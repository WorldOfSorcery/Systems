package me.hektortm.woSSystems.utils;

public enum Letters {

    A("\uE400"),
    B("\uE401"),
    C("\uE402"),
    D("\uE403"),
    E("\uE404"),
    F("\uE405"),
    G("\uE406"),
    H("\uE407"),
    I("\uE408"),
    J("\uE409"),
    K("\uE410"),
    L("\uE411"),
    M("\uE412"),
    N("\uE413"),
    O("\uE414"),
    P("\uE415"),
    Q("\uE416"),
    R("\uE417"),
    S("\uE418"),
    T("\uE419"),
    U("\uE420"),
    V("\uE421"),
    W("\uE422"),
    X("\uE423"),
    Y("\uE424"),
    Z("\uE425"),
    ZERO("\uE426"),
    ONE("\uE427"),
    TWO("\uE428"),
    THREE("\uE429"),
    FOUR("\uE430"),
    FIVE("\uE431"),
    SIX("\uE432"),
    SEVEN("\uE433"),
    EIGHT("\uE434"),
    NINE("\uE435"),
    UNDERSCORE("\uE436"),
    DASH("\uE437"),
    QUOTE("\uE438"),
    AMPERSAND("\uE439"),
    BRACKET_OPEN("\uE440"),
    BRACKET_CLOSED("\uE441"),
    COLON("\uE442"),
    EQUALS("\uE443"),
    EXCLAMATION("\uE444"),
    HASHTAG("\uE445"),
    PLUS("\uE446"),
    QUESTION("\uE447"),
    SLASH("\uE448"),
    SEMICOLON("\uE449"),
    PERCENTAGE("\uE450"),
    DOT("\uE451"),
    COMMA("\uE452"),
    STAR("\uE453");

    Letters(String letter) {
        this.letter = letter;
    };

    private final String letter;

    public String getLetter() {
        return letter;
    }
}
