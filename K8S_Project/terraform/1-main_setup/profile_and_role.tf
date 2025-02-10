
resource "aws_iam_instance_profile" "k3s_node_instance_profile" {
  name = "k3s_node_instance_profile"
  role = aws_iam_role.k3s_node_role.name
}

resource "aws_iam_role" "k3s_node_role" {
  name = "k3s_node_role"
  tags = {
    Name = "K3s_Node_Role"
  }

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
			"Action": [
				"ec2:*",
				"s3:*",
				"iam:CreateRole",
				"iam:DeleteRole",
				"iam:AttachRolePolicy",
				"iam:DetachRolePolicy",
				"iam:PutRolePolicy",
				"iam:ListAttachedRolePolicies",
				"iam:ListRolePolicies",
				"iam:ListRoleTags",
				"iam:GetRole",
				"iam:GetRolePolicy",
				"iam:PassRole",
				"iam:ListRoles",
				"iam:DeleteRolePolicy",
				"iam:CreateInstanceProfile",
				"iam:ListInstanceProfilesForRole",
				"iam:GetInstanceProfile",
				"iam:DeleteRole",
				"iam:DeleteInstanceProfile",
				"iam:AddRoleToInstanceProfile",
				"iam:RemoveRoleFromInstanceProfile",
				"iam:TagRole",
				"iam:GetPolicy",
				"iam:ListPolicies",
				"autoscaling:Describe*",
				"autoscaling:UpdateAutoScalingGroup",
				"autoscaling:CreateAutoScalingGroup",
				"autoscaling:DeleteAutoScalingGroup",
				"ssm:GetParameter",
				"ssm:GetParameters",
				"ssm:DescribeParameters",
				"ssm:PutParameter",
				"ssm:ListTagsForResource",
				"ssm:DeleteParameter"
			],
			"Effect": "Allow",
			"Resource": "*"
		}

    ]
  })
}

resource "aws_iam_role_policy_attachment" "k3s_nlb_policy_attachment" {
  role       = aws_iam_role.k3s_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/ElasticLoadBalancingFullAccess"
}

resource "aws_iam_role_policy_attachment" "k3s_route53_policy_attachment" {
  role       = aws_iam_role.k3s_node_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonRoute53FullAccess"
}
