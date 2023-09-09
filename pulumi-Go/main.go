package main
import (
	"github.com/pulumi/pulumi-aws/sdk/v6/go/aws/s3"
	"github.com/pulumi/pulumi/sdk/v3/go/pulumi"
)


// Create a Vpc 
func main() {
	pulumi.Run(func(ctx *pulumi.Context) error {
		_, err := ec2.NewVpc(ctx, "myvpc", &ec2.VpcArgs{
			CidrBlock: pulumi.String("10.1.0.0/16"),
		})
		if err != nil {
			return err
		}
	}

// Create a Subnet
		_, err := ec2.NewSubnet(ctx, "main", &ec2.SubnetArgs{
			VpcId:     pulumi.Any(aws_vpc.myvpc.Id),
			CidrBlock: pulumi.String("10.1.10.0/24"),
		})
		if err != nil {
			return err
		}
	}
}

// Create an Internet Gateway
		_, err := ec2.NewInternetGateway(ctx, "myinternetgateway", &ec2.InternetGatewayArgs{
			VpcId: pulumi.Any(aws_vpc.myvpc.Id),
			Tags: pulumi.StringMap{
			})
			if err != nil {
				return err
			}

// Cretae an Route Table 
		_, err := ec2.NewRouteTable(ctx, "myroutetable", &ec2.RouteTableArgs{
			VpcId: pulumi.Any(aws_vpc.myvpc.Id),
			Routes: ec2.RouteTableRouteArray{
				&ec2.RouteTableRouteArgs{
					CidrBlock: pulumi.String("0.0.0.0/0"),
					GatewayId: pulumi.Any(aws_internet_gateway.myinternetgateway.Id)
				})
				if err != nil {
					return err
				}
				
// Create an Route Table Association 
		_, err := ec2.NewRouteTableAssociation(ctx, "routeTableAssociation", &ec2.RouteTableAssociationArgs{
			SubnetId:     pulumi.Any(aws_subnet.mysubnet.Id),
			RouteTableId: pulumi.Any(aws_route_table.myroutetable.Id),
		})
		if err != nil {
			return err
		}
// Create a Security Group
		_, err := ec2.NewSecurityGroup(ctx, "newsecuritygroup", &ec2.SecurityGroupArgs{
			Description: pulumi.String("Allow TLS inbound traffic"),
			VpcId:       pulumi.Any(aws_vpc.myvpc.Id),
			Ingress: ec2.SecurityGroupIngressArray{
				&ec2.SecurityGroupIngressArgs{
					Description: pulumi.String("TLS from VPC"),
					FromPort:    pulumi.Int(22),
					ToPort:      pulumi.Int(22),
					Protocol:    pulumi.String("tcp"),
					CidrBlocks: pulumi.StringArray{
						aws_vpc.Main.Cidr_block,
					},
					Ipv6CidrBlocks: pulumi.StringArray{
						aws_vpc.Main.Ipv6_cidr_block,
					},
				},
			},
			Egress: ec2.SecurityGroupEgressArray{
				&ec2.SecurityGroupEgressArgs{
					FromPort: pulumi.Int(0),
					ToPort:   pulumi.Int(0),
					Protocol: pulumi.String("-1"),
					CidrBlocks: pulumi.StringArray{
						pulumi.String("0.0.0.0/0"),
					},
					Ipv6CidrBlocks: pulumi.StringArray{
						pulumi.String("::/0"),
					},
				},
			},
			Tags: pulumi.StringMap{
				"Name": pulumi.String("allow_tls"),
			},
		})
		if err != nil {
			return err
		}
// Create an EC2 Instance 
		_, err = ec2.NewInstance(ctx, "web", &ec2.InstanceArgs{
			Ami:          *pulumi.String("ami-053b0d53c279acc90"), // / For example. Replace with your actual AMI id
			InstanceType: pulumi.String("t2.micro"),
			KeyName: pulumi.String("minikube"),
			AvailabilityZone: pulumi.String("us-east-1a"), //  // Make sure this is the same availability zone as your subnet
			SubnetId:          subnet.ID(aws_subnet.mysubnet.Id),
			VpcSecurityGroupIds: pulumi.StringArray{aws_security()},
		})
		if err != nil {
			return err
		}
