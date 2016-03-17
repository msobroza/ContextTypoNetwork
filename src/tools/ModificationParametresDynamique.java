package tools;

import control.ContextTypoNetwork;
import java.io.FileReader;
import java.lang.reflect.Field;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class ModificationParametresDynamique {

    private static JSONObject accederTemplate(String URL) {

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(URL));
            return (JSONObject) obj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void appliquerTemplate(String nomTemplate, Class t, Class r) {
        JSONObject templateCurrent = accederTemplate(nomTemplate);
        if (templateCurrent.containsKey(t.getName())) {
            JSONObject parametresTypo = (JSONObject) templateCurrent.get(t.getName());
            for (Object nomParametre : parametresTypo.keySet()) {
                modifierParametre(t, parametresTypo.get(nomParametre), (String) nomParametre);
            }
        }
        if (templateCurrent.containsKey(r.getName())) {
            JSONObject parametresReseau = (JSONObject) templateCurrent.get(r.getName());
            for (Object nomParametre : parametresReseau.keySet()) {
                modifierParametre(r, parametresReseau.get(nomParametre), (String) nomParametre);
            }
        }
    }

    public static boolean modifierParametre(Class classe, Object contenu, String nomParametre) {

        try {
            Class cls = Class.forName(classe.getName());
            Field fld = cls.getField(nomParametre);
            fld.set(classe, contenu);
            ContextTypoNetwork.logger.debug("Nom du parametre modifié: " + nomParametre + " Contenu: " + fld.get(classe));
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println(e);
            return false;
        } catch (IllegalAccessException e) {
            System.err.println(e);
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println(e);
            return false;
        } catch (NoSuchFieldException e) {
            System.err.println(e);
            return false;
        } catch (SecurityException e) {
            System.err.println(e);
            return false;
        }
    }

}
