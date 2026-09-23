package ai.ml.leetcode.Tree;

public class LowestCommonAncestor {

    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        // Base case: If the root is null or matches one of the target nodes, it's the LCA.
        if (root == null || root == p || root == q) {
            return root;
        }
        // Recursively search in the left and right subtrees.
        TreeNode leftLCA = lowestCommonAncestor(root.left, p, q);
        TreeNode rightLCA = lowestCommonAncestor(root.right, p, q);

        // If both leftLCA and rightLCA are not null, it means p and q are in different subtrees,
        // so the current root is the LCA.
        if (leftLCA != null && rightLCA != null) {
            return root;
        }
        // If only leftLCA is not null, it means both p and q (or one of them) are in the left subtree.
        else if (leftLCA != null) {
            return leftLCA;
        }
        // If only rightLCA is not null, it means both p and q (or one of them) are in the right subtree.
        else {
            return rightLCA;
        }
    }

    public static void main(String[] args) {
        // Example usage:
        // Construct a sample binary tree
        TreeNode root = new TreeNode(3);
        root.left = new TreeNode(5);
        root.right = new TreeNode(1);
        root.left.left = new TreeNode(6);
        root.left.right = new TreeNode(2);
        root.right.left = new TreeNode(0);
        root.right.right = new TreeNode(8);
        root.left.right.left = new TreeNode(7);
        root.left.right.right = new TreeNode(4);

        LowestCommonAncestor finder = new LowestCommonAncestor();

        // Find LCA of nodes 5 and 1
        TreeNode lca1 = finder.lowestCommonAncestor(root, root.left, root.right);
        System.out.println("LCA of 5 and 1: " + lca1.val); // Expected: 3

        // Find LCA of nodes 5 and 4
        TreeNode lca2 = finder.lowestCommonAncestor(root, root.left, root.left.right.right);
        System.out.println("LCA of 5 and 4: " + lca2.val); // Expected: 5

        // Find LCA of nodes 6 and 7
        TreeNode lca3 = finder.lowestCommonAncestor(root, root.left.left, root.left.right.left);
        System.out.println("LCA of 6 and 7: " + lca3.val); // Expected: 5
    }
}

// TC and SC - O(n)