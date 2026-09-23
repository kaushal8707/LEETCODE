package ai.ml.leetcode.Tree;

public class SameTreeSolution {
    static int target;
    public static boolean isSameTree(TreeNode a, TreeNode b){
        if(a == null && b == null){
            return true;
        }
        if(a == null || b == null){
            return false;
        }
        if(a.val != b.val){
            return false;
        }

        boolean lans = isSameTree(a.left, b.left);
        boolean rans = isSameTree(a.right, b.right);
        return lans && rans;
    }

    public static void main(String[] args) {
        // Construct a sample binary tree
        TreeNode a = new TreeNode(1);
        a.left = new TreeNode(2);
        a.right = new TreeNode(3);

        TreeNode b = new TreeNode(1);
        b.left = new TreeNode(2);
        b.right = new TreeNode(3);

        boolean sameTree = isSameTree(a, b);
        System.out.println(sameTree);

        // Another example
        TreeNode c = new TreeNode(1);
        c.left = new TreeNode(2);
        c.right = new TreeNode(3);
        c.left.right = new TreeNode(11);

        TreeNode d = new TreeNode(1);
        d.left = new TreeNode(2);
        d.right = new TreeNode(3);

        boolean sameTree2 = isSameTree(c, d);
        System.out.println(sameTree2);
    }
}

// TC && SC - O(n)