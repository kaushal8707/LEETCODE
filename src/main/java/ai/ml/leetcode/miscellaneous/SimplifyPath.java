package ai.ml.leetcode.miscellaneous;

import java.util.Stack;

public class SimplifyPath {
    public static void main(String[] args) {
        String path1 = "/home//foo/";  // op-   "/home/foo"
        String path2 = "/../";   // op-  "/"
        String path4 = "/home/user/Documents/../Pictures";  // op-  "/home/user/Pictures"
        String path3 = "/home/";   // op-    "/home"
        String simplifyPath = simplifyPath(path4);
        System.out.println(simplifyPath);
    }

    private static String simplifyPath(String path) {
        String[] arr = path.split("/");
        Stack<String> stack = new Stack();
        for(String str : arr){
            if(!stack.isEmpty() && str.equals("..")){
                stack.pop();
            }else if(!str.equals("") && !str.equals(".") && !str.equals("..")){
                stack.push(str);
            }
        }
        StringBuilder sb = new StringBuilder();
        for(String s : stack){
            sb.append("/");
            sb.append(s);
        }
        return sb.toString();
    }
}
