package ai.ml.leetcode.Tree;

public class CountGoodNodesInBT_Solution {

    public static int goodNodes(TreeNode root){
        return helper(root, Integer.MIN_VALUE);
    }

    public static int helper(TreeNode root, int currMax){
        if(root == null){
            return 0;
        }
        int rootAnswer = 0;
        if(root.val >= currMax){
            rootAnswer = 1;
            currMax = root.val;
        }
        int lans = helper(root.left, currMax);
        int rans = helper(root.right, currMax);

        return lans + rans + rootAnswer;
    }

    public static void main(String[] args) {
        // Construct a sample binary tree
        int targetSum = 22;
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(1);
        root.right = new TreeNode(4);
        root.left.left = new TreeNode(3);
        root.right.left = new TreeNode(1);
        root.right.right = new TreeNode(5);
        int goodNodes = goodNodes(root);
        System.out.println(goodNodes);
    }
}

// TC && SC - O(n)