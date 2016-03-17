package control.rules;


public interface LetterInformation {

    public static String SYMBOLES = " \',-.2578_äàabâcãdefgçèhiéjêkëlmnîoïpqñrstôuvöwxyùzûü<>#*";
    public static String SYMBOLES_INSERTION = " 'àabâcdefgçèhiéjêkëlmnîoïpqrstôuvöwxyzûü";
    public static String CONSONNES = "bcçdfghjklmnñpqrstvwxz";
    public static String VOYELLES = "àaâãeèiéêëîoïôuöyùûü";
    public static String[] PONCTUATION={", ","\\. ","! ", "\\? "};
    public static String VOYELLES_A = "àaâã";
    public static String VOYELLES_E = "eèéêë";
    public static String VOYELLES_I = "iîïy";
    public static String VOYELLES_O = "oôö";
    public static String VOYELLES_U = "uùûü";
    public static String NON_LETTRES = " \',-.2578_<>#";
    public static String [] PHONEMES_LIA = {"<",">","##","??","aa","ai","an","au","bb","ch","dd","ee","ei","eu","ff","gg","ii","in","jj","kk","ll","mm","nn","oe","on","oo","ou","pp","rr","ss","tt","un","uu","uy","vv","ww","yy","zz"};
    public static String CARAC_DEBUT = "<";
    public static String CARAC_FIN = ">";
    public static String CARAC_ESP = "#";
    public static String CARAC_EFFAC = "_";

    public enum QType {

        RANDOM, PREV7, PREV3
    };

    public static QType typeQuestion = QType.RANDOM;

    public static int NB_CAS = 20;
    

}
