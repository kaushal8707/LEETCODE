package ai.ml.leetcode.Tree;

public class Solution1 {
    static int target;
    public static boolean hasPathSum(TreeNode root, int targetSum){
        target = targetSum;
        return helper(root, 0);
    }

    private static boolean helper(TreeNode root, int sum) {
        if(root == null){
            return false;
        }
        sum += root.val;     // in case of recursion we should always consider small part
        if(root.left == null && root.right == null){
            return sum == target;     // imp is do always check null when left & right both null which mean leaf node and reached to end of tree
        }
        boolean lans = helper(root.left, sum);
        boolean rans = helper(root.right, sum);
        return lans || rans;
    }

    public static void main(String[] args) {
        // Construct a sample binary tree
        int targetSum = 22;
        TreeNode root = new TreeNode(5);
        root.left = new TreeNode(4);
        root.right = new TreeNode(8);
        root.left.left = new TreeNode(11);
        root.left.left.left = new TreeNode(7);
        root.left.left.right = new TreeNode(2);
        root.right.left = new TreeNode(13);
        root.right.right = new TreeNode(4);
        root.right.right.right = new TreeNode(1);
        boolean hasPathSum = hasPathSum(root, targetSum);
        System.out.println(hasPathSum);

        // Another example
        targetSum = 5; // Path 1 -> 2 is 3, Path 1 -> 3 is 4. No path with sum 5.
        TreeNode root2 = new TreeNode(1);
        root2.left = new TreeNode(2);
        root2.right = new TreeNode(3);
        boolean hasPathSum2 = hasPathSum(root, targetSum);
        System.out.println(hasPathSum2);
    }
}
