package control.rules;


public interface LetterInformation {

    public static String SYMBOLS = " \',-.2578_äàabâcãdefgçèhiéjêkëlmnîoïpqñrstôuvöwxyùzûü<>#*";
    public static String SYMBOLS_INSERTION = " 'àabâcdefgçèhiéjêkëlmnîoïpqrstôuvöwxyzûü";
    public static String CONSONANTS = "bcçdfghjklmnñpqrstvwxz";
    public static String VOYELLES = "àaâãeèiéêëîoïôuöyùûü";
    public static String[] PONCTUATION_SYMBOLS={", ","\\. ","! ", "\\? "};
    public static String VOWELS_A = "àaâã";
    public static String VOWELS_E = "eèéêë";
    public static String VOWELS_I = "iîïy";
    public static String VOWELS_O = "oôö";
    public static String VOWELS_U = "uùûü";
    public static String NOT_LETTERS = " \',-.2578_<>#";
    public static String [] PHONEMES_LIA = {"<",">","##","??","aa","ai","an","au","bb","ch","dd","ee","ei","eu","ff","gg","ii","in","jj","kk","ll","mm","nn","oe","on","oo","ou","pp","rr","ss","tt","un","uu","uy","vv","ww","yy","zz"};
    public static String BEGIN_WORD_SYMBOL = "<";
    public static String END_WORD_SYMBOL = ">";
    public static String PADDING_SYMBOL = "*";
    public static String SPECIAL_SYMBOL = "#";
    public static String ERASURE_SYMBOL = "_";
    public static String UNKNOWN_WORD_SYMBOLS = "##";
    public static String CONCAT_SYMBOL="|||";
    public static int NB_CAS = 20;
    

}
