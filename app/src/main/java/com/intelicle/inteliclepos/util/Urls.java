package com.intelicle.inteliclepos.util;

import android.util.Log;

public class Urls {

    private static String token;
    private static String companyId;
    private static String outletId;
    private static String[] urlList = new String[]{"/api/App/Login",
                                                   "/api/App/UpdateItem",
                                                   "/api/App/AddItem",
                                                   "/api/App/ItemLookups",
                                                   "/api/App/ScanItem"};
    private static String domain;

    public static enum Type {
        LOGIN, UPDATE, ADD, SHOW, SCAN, SCAN2
    }

    //this method will set token
    public static void setToken(String token){
        Urls.token = token;
    }

    //this method will get token
    public static String getToken(){
        if (token!=null)
            return Urls.token;

        return null;
    }

    //this method will take domain argument
    public static boolean setDomain(String domain){
        Urls.domain = "http://"+domain;
        //Urls.domain = domain;
        return true;
    }

    public static String getDomain(){
        if (domain!=null){
            return domain;
        }
        return null;
    }

    //this method will return domain existence
    public static boolean isDomainAvailable(){
        if (domain!=null){
            return true;
        }
        return false;
    }

    //this method will concat with different url according to need and return
    public static String getUrl(Type type){
        if (domain!=null){
            if (type==Type.LOGIN){
                 return domain+urlList[0];
            }else if (type==Type.UPDATE){
                return domain+urlList[1];
            }else if (type==Type.ADD){
                return domain+urlList[2];
            }else if (type==Type.SHOW){
                return domain+urlList[3];
            }else if (type==Type.SCAN || type==Type.SCAN2){
                return domain+urlList[4];
            }
        }
        return null;
    }

    public static String getRequestMethod(Type type){
        if (type==Type.LOGIN){
            return "POST";
        }else if (type==Type.UPDATE){
            return "PUT";
        }else if (type==Type.ADD){
            return "POST";
        }else if (type==Type.SHOW){
            return "GET";
        }else if (type==Type.SCAN || type==Type.SCAN2){
            return "GET";
        }
        return null;
    }

    public static void setCompanyId(String companyId){
        Log.v("login","c"+companyId);
        Urls.companyId = companyId;
    }

    public static void setOutletId(String outletId){
        Log.v("login","c"+outletId);
        Urls.outletId =  outletId;
    }

    public static String getCompanyId(){
        return Urls.companyId;
    }

    public static String getOutletId(){
        return Urls.outletId;
    }
}