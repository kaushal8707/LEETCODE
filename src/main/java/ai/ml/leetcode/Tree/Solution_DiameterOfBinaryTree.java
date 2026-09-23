package ai.ml.leetcode.Tree;

public class Solution_DiameterOfBinaryTree {
    private int diameter = 0; // Stores the maximum diameter found so far
        public int diameterOfBinaryTree(TreeNode root) {
            diameter = 0; // Reset diameter for each new call
            calculateDepth(root);
            return diameter;
        }
    // This function calculates the depth of a node and updates the global diameter
    private int calculateDepth(TreeNode node) {
        if (node == null) {
            return 0;
        }

        int leftDepth = calculateDepth(node.left);
        int rightDepth = calculateDepth(node.right);

        // Update the diameter: the diameter through the current node
        // is the sum of the depths of its left and right subtrees.
        // We take the maximum of this and the current maximum diameter.
        diameter = Math.max(diameter, leftDepth + rightDepth);

        // Return the depth of the current subtree for its parent
        return 1 + Math.max(leftDepth, rightDepth);
    }


    public static void main(String[] args) {
        Solution_DiameterOfBinaryTree solver = new Solution_DiameterOfBinaryTree();
        // Example 1: Simple tree
        TreeNode root1 = new TreeNode(1);
        root1.left = new TreeNode(2);
        root1.right = new TreeNode(3);
        root1.left.left = new TreeNode(4);
        root1.left.right = new TreeNode(5);
        System.out.println("Min depth of tree 1: " + solver.diameterOfBinaryTree(root1)); // Expected: 2 (path 3 -> 9)
    }
}
