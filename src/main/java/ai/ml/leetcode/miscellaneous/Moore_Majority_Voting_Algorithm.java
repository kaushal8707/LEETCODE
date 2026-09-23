package ai.ml.leetcode.miscellaneous;

public class Moore_Majority_Voting_Algorithm {
    public static void main(String[] args) {
        int nums[] = {2,2,1,1,1,2,2};  // 3,2,3
        int majorityElements = majorityElements(nums);
        System.out.println(majorityElements);
    }

    private static int majorityElements(int[] nums) {
        int candidate = nums[0];
        int count = 0;

        for(int i : nums){
            if(i == candidate){
                count++;
            }else{
                count--;
                if(count == 0){
                    candidate = i;
                    count++;
                }
            }
        }
        return candidate;
    }
}

// TC - O(n)
// SC - O(1)