package maf.isenframework.isenframeworkapplication;

import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import maf.isenframework.isenframeworkapplication.beans.ApplicationBean;
import maf.isenframework.isenframeworkapplication.beans.LoginBean;
import maf.isenframework.isenframeworkapplication.beans.UserBean;
import maf.isenframework.isenframeworkapplication.dao.ApplicationDao;
import maf.isenframework.isenframeworkapplication.data.Application;
import maf.isenframework.isenframeworkapplication.data.User;

import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.event.Event;
import oracle.adfmf.framework.event.EventListener;
import oracle.adfmf.framework.exception.AdfException;
import oracle.adfmf.util.Utility;

/**
 * Listener of URL scheme event.
 */
public class UrlSchemeListener implements EventListener {
    public UrlSchemeListener() {
        super();
    }

    @Override
    public void onMessage(Event event) {
        String url = event.getPayload();
        Utility.ApplicationLogger.info("URL: " + url);
        
        //alert("URL: " + url);
        
        Map<String, String> params;
        try {
            params = parseQuery(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        LoginBean loginBean = (LoginBean) AdfmfJavaUtilities.getELValue("#{LoginBean}");
        UserBean userBean = (UserBean) AdfmfJavaUtilities.getELValue("#{UserBean}");
        
        if ("register".equalsIgnoreCase(params.get("action"))) { // ictframework://?action=register&name=Test&scheme=test
            try {
                String scheme = params.get("scheme");
                ApplicationDao dao = new ApplicationDao();
                List<Application> apps = dao.getByUrlScheme(scheme);
                if (apps.size() == 0) {
                    Application app = new Application(params.get("name"), scheme, null);
                    dao.add(app);
                    alert("Application added.");
                } else {
                    alert("Application with scheme " + scheme + " already registered!");
                }
            } catch (Exception e) {
                alert("Adding failed: "+e.getMessage());
                e.printStackTrace();
            }
        }
        
        if ("true".equalsIgnoreCase(params.get("afterGesture"))) {
            // was gesture already
            //alert("after gesture");
            //AdfmfContainerUtilities.invokeContainerJavaScriptFunction("iCTFramework", "adf.mf.api.amx.doNavigation", new Object[] { "__back" });
        
        } else if ("true".equalsIgnoreCase(params.get("fromManage")) || "true".equalsIgnoreCase(params.get("becauseNotInKiosk"))) {
            // escaped management
            //alert("from manage");
            //AdfmfContainerUtilities.invokeContainerJavaScriptFunction("iCTFramework", "adf.mf.api.amx.doNavigation", new Object[] { "__back" });
        
        } else if (loginBean.getLogged().getCanEscapeApp() && loginBean.getLogged().equals(userBean.getCurrent())) {
            // user can escape (and it is he - not started app for client)
            //alert("allowed escape");
            //AdfmfContainerUtilities.invokeContainerJavaScriptFunction("iCTFramework", "adf.mf.api.amx.doNavigation", new Object[] { "__back" });
        
        } else {
            // other - logout
            //alert("logging out");
            loginBean.logOut(null);
        }
    }
    
    private void alert(String message) {
        AdfmfContainerUtilities.invokeContainerJavaScriptFunction("iCTFramework", "alert", new Object[] { message });
    }

    @Override
    public void onOpen(String id) {}

    @Override
    public void onError(AdfException error) {
        Utility.ApplicationLogger.warning("URL error: " + error.toString());
        AdfmfContainerUtilities.invokeContainerJavaScriptFunction("iCTFramework", "alert", new Object[] {"Chyba URL schématu!"});
    }
    
    private static Map<String, String> parseQuery(String url) throws UnsupportedEncodingException {
        Map<String, String> params = new LinkedHashMap<String, String>();
        int begin = url.indexOf("?");
        if (begin == -1) return Collections.emptyMap();
        String query = url.substring(url.indexOf("?") + 1);
        for (String paramString : query.split("&")) {
            int delimiter = paramString.indexOf("=");
            if (delimiter == -1) continue;
            String name = URLDecoder.decode(paramString.substring(0, delimiter), "UTF-8");
            String value = URLDecoder.decode(paramString.substring(delimiter + 1), "UTF-8");
            params.put(name, value);
        }
        return params;
    }
}
