import pulumi
import pulumi_aws as aws

# Configuring AWS access key and secret key
config = pulumi.Config()
aws_access_key = config.require("AKIAVH34WU7JTJ3URI53")
aws_secret_key = config.require("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")

# Set AWS credentials
aws.config.credentials = aws.Credentials(aws_access_key=aws_access_key, aws_secret_key=aws_secret_key)

# Create a VPC
vpc = aws.ec2.Vpc("example_vpc",
    cidr_block="10.1.0.0/16")

# Create a Public subnet
public_subnet = aws.ec2.Subnet("publicsubnet",
    vpc_id=vpc.id,
    cidr_block="10.1.10.0/24",
    availability_zone="us-east-1a")

# Create a Private subnet
private_subnet = aws.ec2.Subnet("privatesubnet",
    vpc_id=vpc.id,
    cidr_block="10.1.20.0/24",
    availability_zone="us-east-1b")

# Create a Private1 subnet
private_subnet1 = aws.ec2.Subnet("privatesubnet1",
    vpc_id=vpc.id,
    cidr_block="10.1.30.0/24",
    availability_zone="us-east-1c")

# Create an Internet Gateway
my_igw = aws.ec2.InternetGateway("myigw",
    vpc_id=vpc.id)

# Create a Public route table
public_route_table = aws.ec2.RouteTable("publicroutetable",
    vpc_id=vpc.id)

# Create a Private route table
private_route_table = aws.ec2.RouteTable("privateroutetable",
    vpc_id=vpc.id)

# Create a Private1 route table
private_route_table1 = aws.ec2.RouteTable("privateroutetable1",
    vpc_id=vpc.id)

# Create a Private1 route
private_route1 = aws.ec2.Route("publicroute1",
    route_table_id=private_route_table1.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=my_igw.id)

# Create a Public route
public_route = aws.ec2.Route("publicroute",
    route_table_id=public_route_table.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=my_igw.id)

# Create a Private route
private_route = aws.ec2.Route("privatecroute",
    route_table_id=private_route_table.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=my_igw.id)

# Create a Public subnet association
public_association = aws.ec2.RouteTableAssociation("publicassociation",
    subnet_id=public_subnet.id,
    route_table_id=public_route_table.id)

# Create a private subnet association
private_association = aws.ec2.RouteTableAssociation("privatecassociation",
    subnet_id=private_subnet.id,
    route_table_id=private_route_table.id)

# Create a private subnet1 association
private_association1 = aws.ec2.RouteTableAssociation("privatecassociation1",
    subnet_id=private_subnet1.id,
    route_table_id=private_route_table1.id)

# Create an Internet gateway
my_igw = aws.ec2.InternetGateway("myigw",
    vpc_id=vpc.id)

# Create a Security group
example_sg = aws.ec2.SecurityGroup("example_sg",
    name="my-sg",
    description="For the infra",
    vpc_id=vpc.id,
    ingress=[
        {
            "from_port": 22,
            "to_port": 22,
            "protocol": "tcp",
            "cidr_blocks": ["10.1.0.0/0"]
        }
    ],
    egress=[
        {
            "from_port": 0,
            "to_port": 0,
            "protocol": "-1",
            "cidr_blocks": ["0.0.0.0/0"]
        }
    ])

# Create an EBS volume
my_ebs = aws.ebs.Volume("myebs",
    availability_zone="us-east-1a",
    size=8,
    encrypted=True,
    tags={
        "Name": "Example EBS Volume"
    })

# Create a snapshot from the EBS volume
my_snapshot = aws.ebs.Snapshot("mysnapshot",
    volume_id=my_ebs.id,
    tags={
        "Name": "Example Snapshot"
    })

# Create an AMI from the snapshot
example_ami = aws.ec2.Ami("example",
    name="terraform-example",
    virtualization_type="hvm",
    root_device_name="/dev/xvda",
    imds_support="v2.0",
    ebs_block_device=[
        {
            "device_name": "/dev/xvda",
            "snapshot_id": my_snapshot.id,
            "volume_size": 8
        }
    ])

# Declare your Launch Template resource
my_template = aws.ec2.LaunchTemplate("mytemplate",
    name="example-launch-template",
    image_id="ami-08a52ddb321b32a8c",  # Replace with the desired AMI ID
    instance_type="t2.micro",
    vpc_security_group_ids=[example_sg.id])

# Create Target group for Loadbalancer
new_target_group = aws.lb.TargetGroup("newtarget_group",
    name="newtarget-group",
    port=80,
    protocol="HTTP",
    vpc_id=vpc.id)

# Create Loadbalancer
new_alb = aws.lb.LoadBalancer("newalb",
    name="new-alb",
    internal=True,
    drop_invalid_header_fields=True,
    load_balancer_type="application",
    security_groups=[example_sg.id],
    subnets=[private_subnet1.id, private_subnet.id])

# Add a listener for the load balancer to forward traffic to the target group
my_listener = aws.lb.Listener("mylistener",
    load_balancer_arn=new_alb.arn,
    port=80,
    protocol="HTTP",
    default_action={
        "target_group_arn": new_target_group.arn,
        "type": "forward"
    })

# Create an Auto Scaling Group
my_aug = aws.autoscaling.Group("myaug",
    name="myaug",
    launch_template={
        "id": my_template.id,
        "version": "$Latest"
    },
    target_group_arns=[new_target_group.arn],  # Attach the target group to the ASG
    vpc_zone_identifiers=[private_subnet.id, private_subnet1.id],
    min_size=1,
    max_size=2,
    desired_capacity=1,
    health_check_type="ELB")  # Use "EC2" for EC2 instance health check

# Exporting outputs
pulumi.export("vpc_id", vpc.id)
pulumi.export("public_subnet_id", public_subnet.id)
pulumi.export("private_subnet_id", private_subnet.id)
pulumi.export("private_subnet1_id", private_subnet1.id)
pulumi.export("internet_gateway_id", my_igw.id)
pulumi.export("example_sg_id", example_sg.id)
pulumi.export("my_ebs_id", my_ebs.id)
pulumi.export("my_snapshot_id", my_snapshot.id)
pulumi.export("example_ami_id", example_ami.id)
pulumi.export("my_template_id", my_template.id)
pulumi.export("new_target_group_id", new_target_group.id)
pulumi.export("new_alb_id", new_alb.id)
pulumi.export("my_listener_id", my_listener.id)
pulumi.export("my_aug_id", my_aug.id)

