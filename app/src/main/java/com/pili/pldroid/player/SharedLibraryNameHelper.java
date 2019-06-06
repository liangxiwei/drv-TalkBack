//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.pili.pldroid.player;

public class SharedLibraryNameHelper {
//    private String a;

    private SharedLibraryNameHelper() {
//        this.a = "pldroidplayer";
    }

    public static SharedLibraryNameHelper getInstance() {
        return SharedLibraryNameHelper.newHelper.a;
    }

//    public void renameSharedLibrary(String var1) {
//        Log.i("SharedLibraryNameHelper", "renameSharedLibrary newName:" + var1);
//        this.a = var1;
//    }
//
//    public String getSharedLibraryName() {
//        return this.a;
//    }

    public void a() {
//        if(this.a.contains("/")) {
//            System.load(this.a);
//        } else {
//            System.loadLibrary(this.a);
//        }
//        System.loadLibrary("pldroidplayer");
        System.loadLibrary("ijkffmpeg");
        System.loadLibrary("ijksdl");
        System.loadLibrary("ijkplayer");
    }

    private static class newHelper {
        public static final SharedLibraryNameHelper a = new SharedLibraryNameHelper();
    }
}
