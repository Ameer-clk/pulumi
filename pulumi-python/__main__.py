import pulumi
import pulumi_aws as aws

# Create Vpc
my_vpc = aws.ec2.Vpc("my_vpc", # Define you vpc name 
    cidr_block="10.1.0.0/16",)

# Create Subnet
public_subnet = aws.ec2.Subnet("public_subnet", #Define your subnet name 
    vpc_id=my_vpc.id,
    availability_zone='us-east-1a',
    cidr_block="10.1.10.0/24",)

# Create Subnet
private_subnet = aws.ec2.Subnet("private_subnet", # Define your subnet name 
    vpc_id=my_vpc.id,
    availability_zone='us-east-1b',
    cidr_block="10.1.20.0/24",)

# Create Internet gateway
internet_gateway = aws.ec2.InternetGateway("internet_gateway", 
    vpc_id=my_vpc.id,)

 # Create Public Route table 
public_route_table = aws.ec2.RouteTable("public_route_table",
    vpc_id=my_vpc.id,)

# Creare  Private Route table 
private_route_table = aws.ec2.RouteTable("private_rouet_table",
    vpc_id=my_vpc.id,)

# Create Public route table association
public_route_table_association = aws.ec2.RouteTableAssociation("public_routeTableAssociation",
    subnet_id=public_subnet.id,
    route_table_id=public_route_table.id,)

# Create Private route table association
private_route_table_association = aws.ec2.RouteTableAssociation("private_routeTableAssociation",
    subnet_id=private_subnet.id,
    route_table_id=private_route_table.id,)

 # Create Public route
public_route = aws.ec2.Route("public_route",
    route_table_id=public_route_table.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=internet_gateway,)

# Create Private route
private_route = aws.ec2.Route("private_route",
    route_table_id=private_route_table.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=internet_gateway.id,)


# Create Security group
security_group = aws.ec2.SecurityGroup("security_group",
    description="Allow TLS inbound traffic",
    vpc_id=my_vpc.id,)

ingrees =[aws.ec2.SecurityGroupIngressArgs(
        description="TLS from VPC",
        from_port=22,
        to_port=22,
        protocol="tcp",
        cidr_blocks=["0.0.0.0/0"]
    )],
egress=[aws.ec2.SecurityGroupEgressArgs(
        from_port=0,
        to_port=0,
        protocol="-1",
        cidr_blocks=["0.0.0.0/0"],
    )],

# Create EBS Volume
newvolume = aws.ebs.Volume("newvolume",
    availability_zone="us-east-1a",
    size=8,)

# Create EBS Snapshot
newvolume_snapshot = aws.ebs.Snapshot("newvolumeSnapshot",
    volume_id=newvolume.id,)

# Create an EC2 Instance
web_instance = aws.ec2.Instance("webInstance",
    ami='ami-08a52ddb321b32a8c',
    instance_type="t2.small",
    subnet_id=private_subnet.id,
    availability_zone='us-east-1b',
    key_name='minikube',
    vpc_security_group_ids=[security_group.id], # Be sure to replace `my_sg` with your actual Security Group resource
)
# Create a Volume attachment for EC2 Instance
volume_attachment = aws.ec2.VolumeAttachment("volume_attachment",
    device_name="/dev/sdf",  # Change this to the appropriate device name
    instance_id=web_instance.id,
    volume_id=newvolume.id
)

    
    
