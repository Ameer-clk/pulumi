package generated_program;

import com.pulumi.Pulumi;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.VpcArgs;
import com.pulumi.aws.ec2.Subnet;
import com.pulumi.aws.ec2.SubnetArgs;
import com.pulumi.aws.ec2.InternetGateway;
import com.pulumi.aws.ec2.InternetGatewayArgs;
import com.pulumi.aws.ec2.RouteTable;
import com.pulumi.aws.ec2.RouteTableArgs;
import com.pulumi.aws.ec2.inputs.RouteTableRouteArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.aws.ebs.Volume;
import com.pulumi.aws.ebs.VolumeArgs;
import com.pulumi.aws.ebs.Snapshot;
import com.pulumi.aws.ebs.SnapshotArgs;
import com.pulumi.aws.ec2.Ami;
import com.pulumi.aws.ec2.AmiArgs;
import com.pulumi.aws.ec2.inputs.AmiEbsBlockDeviceArgs;
import com.pulumi.aws.ec2.LaunchTemplate;
import com.pulumi.aws.ec2.LaunchTemplateArgs;
import com.pulumi.aws.elb.LoadBalancer;
import com.pulumi.aws.elb.LoadBalancerArgs;
import com.pulumi.aws.elb.inputs.LoadBalancerAccessLogsArgs;
import com.pulumi.aws.elb.inputs.LoadBalancerListenerArgs;
import com.pulumi.aws.lb.TargetGroup;
import com.pulumi.aws.lb.TargetGroupArgs;
import com.pulumi.aws.autoscaling.Group;
import com.pulumi.aws.autoscaling.GroupArgs;


public class App {
    public static void main(String[] args) {
        Pulumi.run(App::stack);
    }

// Create a vpc
 Vpc my_vpc = new Vpc("myVpc", VpcArgs.build()        
.cidrBlock("10.1.0.0/16")
.build());

// Create a public subnet
Subnet public_subnet = new Subnet("publicSubnet", SubnetArgs.builder()
.vpcId(myVpc.id)
.cidrBlock("10.1.10.0/24")
.availabilityZone("us-east-1a")
.build());

 // Create a Private subnet
 Subnet private_subnet = new Subnet("privateSubnet", SubnetArgs.builder()
    .vpcId(myVpc.id)
    .cidrBlock("10.1.20.0/24")
    .availabilityZone("us-east-1b")
    .build());

 // Create a private subnet
Subnet private_subnet1 = new Subnet("privateSubnet1", SubnetArgs.builder()
.vpcId(myVpc.id)
.cidrBlock("10.1.30.0/24")
.availabilityZone("us-east-1c")
.builder());

 // Create a internet gateway
InternetGateway internet_gateway = new InternetGateway("InternetGateway", InternetGatewayArgs.builder()
.vpcId(myVpc.id)
.build());

 // Create public route table and attaching public route table routes
Routetable public_route_table = new RouteTable("publicroutetable", RouteTableArgs.builder()
            .vpcId(myVpc.id))
            .routes(
                RouteTableRouteArgs.builder()
                    .cidrBlock("0.0.0.0/0")
                    .gatewayId(igw.id)
                    .build());

 // Create Private route table with attaching pirvate route in to it
Routetable private_route_table = new RouteTable("privateroutetable", RouteTableArgs.builder()
.vpcId(myVpc.id))
.route(
    RouteTableRouteArgs.builder()
    .cidrBlock("0.0.0.0/0")
    .gatewayId(InternetGateway.id)
    .build());

// Create Security group
SecurityGroup secuirty_group = new SecurityGroupRule("SecurityGroup", SecurityGroupRuleArgs.builder()
            .type("ingress")
            .fromPort(22)
            .toPort(22)
            .protocol("tcp")
            .cidrBlocks("0.0.0.0/0")
            .type("egress")
            .fromPort(22)
            .toPort(22)
            .protocol("tcp")
            .cidrBlocks("0.0.0.0/0")
            .type("egress")
            .toPort(0)
            .protocol("-1")
            .fromPort(0)
            .cidrBlocks("0.0.0.0/0")
            .build()
            .build());

 // Create EBS Volume and Volume Snapshot
Volume new_volume = new Volume("newvolume", VolumeArgs.builder()
.availabilityZone("us-east-1a")
.size(8)
.build());

// Create a EBS Snapshot
Snapshot exampleSnapshot = new Snapshot("exampleSnapshot", SnapshotArgs.builder()
.volumeId(newvolume.id)
.tags(Map.of("Name", "HelloWorld_snap"))
.build());

 // Create an AMI Image
Ami example = new Ami("example", AmiArgs.builder()
.ebsBlockDevices(AmiEbsBlockDeviceArgs.builder()
    .deviceName("/dev/xvda")
    .snapshotId("exampleSnapshot")
    .volumeSize(8)
    .build())
    .imdsSupport("v2.0")
    .rootDeviceName("/dev/xvda")
    .virtualizationType("hvm")
    .build());

   // Create a Launch Template
 LaunchTemplate new_template = new LaunchTemplate("newTemplate", LaunchTemplateArgs.builder()
     .imageId("ami-08a52ddb321b32a8c")
     .instanceType("t2.micro")
     .keyName("minikube")
     .placement(LaunchTemplatePlacementArgs.builder()
         .availabilityZone("us-east-1a")
         .build())
     .networkInterfaces(LaunchTemplateNetworkInterfaceArgs.builder()
         .securityGroups(SecurityGroup.id)
         .associatePublicIpAddress(true)
         .build())
     .disableApiTermination(true)
     .build());

// Create LoadBalancer Target group
TargetGroup test_target = new TargetGroup("testtarget", TargetGroupArgs.builder()
.port(80)
.protocol("HTTP")
.vpcId(myVpc.id)
.build());

 // Create an Application LoadBalancer
 LoadBalancer application_balancer = new LoadBalancer("applicationbalancer", LoadBalancerArgs.builder()
.internal(true)
.LoadBalancertype("application")
.SecurityGroup(InternetGateway.id)
.Subnet(privateSubnet.id,privateSubnet1.id))
.build();

 // Create an LoadBalancer Lister for Application LoadBalancer
Listener Application_Listener = new Listener("applicationListener", ListenerArgs.builder()
.LoadBalancerArn(applicationbalancer)
.port(80)
.protocol("HTTP")
.defaultActions(ListenerDefaultActionArgs.builder(),
type("forward")
            .targetGroupArn(testtarget.arn())
            .build())
            .build());

 // Create an Autosaclling group
AutoScalling Autosaclling_group = new Group("autoscallinggroup", GroupArgs.builder()
.availabilityZones("us-east-1a")
.desiredCapacity(1)
.maxSize(2)
.minSize(1)
.launchTemplate(GroupLaunchTemplateArgs.builder()
    .id(newTemplate.id)
    .version("$Latest")
    .build())
.build());
}