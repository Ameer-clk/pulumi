import pulumi
import pulumi_aws as aws

# Create vpc
vpc = aws.ec2.Vpc("my_vpc",
    cidr_block="172.31.0.0/16")

# Create public subnet
public_subnet = aws.ec2.Subnet("public_subnet",
    vpc_id=vpc.id,
    availability_zone='us-east-1a',
    cidr_block="172.31.10.0/24")

# Create private subnet
private_subnet = aws.ec2.Subnet("private_subnet",
    vpc_id=vpc.id,
    availability_zone='us-east-1b',
    cidr_block="172.31.20.0/24")

# Create InternetGateway
internet_gateway = aws.ec2.InternetGateway("my_ig",
    vpc_id=vpc.id)

# Create RouteTable
public_route_table = aws.ec2.RouteTable("public_route_table",
    vpc_id=vpc.id)

private_route_table = aws.ec2.RouteTable("private_route_table",
    vpc_id=vpc.id)

# Create Public route
public_route = aws.ec2.Route("public_route",
    route_table_id=public_route_table.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=internet_gateway.id)

# Create private route
private_route = aws.ec2.Route("private_route",
    route_table_id=private_route_table.id,
    destination_cidr_block="0.0.0.0/0",
    gateway_id=internet_gateway.id)

# Public Route table association
public_association = aws.ec2.RouteTableAssociation("public_association",
    subnet_id=public_subnet.id,
    route_table_id=public_route_table.id)

# Private Route table association
private_association = aws.ec2.RouteTableAssociation("private_association",
    subnet_id=private_subnet.id,
    route_table_id=private_route_table.id)

# Create an AWS Security Group
newsg = aws.ec2.SecurityGroup("newsg",
    description='My Security Group',
    ingress=[aws.ec2.SecurityGroupIngressArgs(
        protocol='tcp',
        from_port=22,
        to_port=22,
        cidr_blocks=['0.0.0.0/0']
    )],
    egress=[aws.ec2.SecurityGroupEgressArgs(
        protocol='-1',
        from_port=0,
        to_port=0,
        cidr_blocks=['0.0.0.0/0']
    )]
)

# Define a new EBS volume
my_volume = aws.ec2.Volume("my_volume",
    availability_zone='us-east-1a',
    size=8,
    encrypted=True
)

# Create a snapshot
my_snapshot = aws.ec2.Snapshot("my_snapshot",
    volume_id=my_volume.id,
    description="This is a snapshot of my EBS volume")

instance = aws.ec2.Instance('my_instance',
    ami='ami-08a52ddb321b32a8c',
    instance_type='t2.micro',
    key_name='minikube',
    availability_zone='us-east-1a',
    vpc_security_group_ids=[newsg.id],
    subnet_id=private_subnet.id,
    root_block_device=[aws.ec2.InstanceRootBlockDeviceArgs(
        volume_id=my_volume.id,
        volume_type="gp2",
        volume_size=8,
    )]
)

# Export the ID of created resources
pulumi.export("vpc_id", vpc.id)
pulumi.export("public_subnet_id", public_subnet.id)
pulumi.export("private_subnet_id", private_subnet.id)
pulumi.export("internet_gateway_id", internet_gateway.id)
pulumi.export("public_route_table_id", public_route_table.id)
pulumi.export("private_route_table_id", private_route_table.id)
pulumi.export("security_group_id", newsg.id)
pulumi.export("instance_id", instance.id_)
pulumi.export("volume_id", my_volume.id)
pulumi.export("snapshot_id", my_snapshot.id)
