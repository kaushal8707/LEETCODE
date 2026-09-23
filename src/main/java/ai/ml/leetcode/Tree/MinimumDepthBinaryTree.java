package ai.ml.leetcode.Tree;

public class MinimumDepthBinaryTree  {
    public int minDepth(TreeNode root) {
        // Base case: If the root is null, the depth is 0.
        if (root == null) {
            return 0;
        }
        // Base case: If it's a leaf node, the depth is 1.
        if (root.left == null && root.right == null) {
            return 1;
        }
        // If only the left child exists, recurse on the left subtree.
        if (root.left != null && root.right == null) {
            return 1 + minDepth(root.left);
        }
        // If only the right child exists, recurse on the right subtree.
        if (root.left == null && root.right != null) {
            return 1 + minDepth(root.right);
        }
        // If both children exist, find the minimum depth of both subtrees
        // and add 1 for the root node.
        int leftDepth = minDepth(root.left);
        int rightDepth = minDepth(root.right);
        return  1 + Math.min(leftDepth, rightDepth);
    }


    public static void main(String[] args) {
        MinimumDepthBinaryTree solver = new MinimumDepthBinaryTree();
        // Example 1: Simple tree
        TreeNode root1 = new TreeNode(3);
        root1.left = new TreeNode(9);
        root1.right = new TreeNode(20);
        root1.right.left = new TreeNode(15);
        root1.right.right = new TreeNode(7);
        System.out.println("Min depth of tree 1: " + solver.minDepth(root1)); // Expected: 2 (path 3 -> 9)

        // Example 2: Tree with only a left child
        TreeNode root2 = new TreeNode(2);
        root2.right = new TreeNode(3);
        root2.right.right = new TreeNode(4);
        root2.right.right.right = new TreeNode(5);
        System.out.println("Min depth of tree 2: " + solver.minDepth(root2)); // Expected: 4 (path 2 -> 3 -> 4 -> 5)

        // Example 3: Empty tree
        TreeNode root3 = null;
        System.out.println("Min depth of tree 3: " + solver.minDepth(root3)); // Expected: 0
    }
}
