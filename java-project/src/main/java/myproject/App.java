package generated_program;

import com.pulumi.Context;
import com.pulumi.Pulumi;
import com.pulumi.core.Output;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.VpcArgs;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.StubNotFoundException;

public class App {
    public static void main(String[] args) {
        Pulumi.run(App::stack);
    }

// Create a vpc
 Vpc my_vpc = new Vpc("My_vpc", VpcArgs.builder()        
.cidrBlock("10.1.0.0/16")
.build());

// Create a public subnet
Subnet public_subnet = new Subnet("public_subnet", SubnetArgs.builder()
.vpcId(my_vpc.id)
.cidrBlock("10.1.10.0/24")
.availabilityZone("us-east-1a")
.build());

 // Create a Private subnet
 Subnet private_subnet = new Subnet("private_subnet", SubnetArgs.builder()
    .vpcId(my_vpc.id)
    .cidrBlock("10.1.20.0/24")
    .availabilityZone("us-east-1b")
    .build());

 // Create a private subnet
Subnet private_subnet1 = new Subnet("private_subnet1", SubnetArgs.builder()
.vpcId(my_vpc.id)
.cidrBlock("10.1.30.0/24")
.availabilityZone("us-east-1c")
.builder());

 // Create a internet gateway
Internetgateway internet_gateway = new InternetGateway("igw", InternetGatewayArgs.builder()
.vpcId(my_vpc.id)
.build());

 // Create public route table and attaching public route table routes
Routetable public_route_table = new RouteTable("publicroutetable", RouteTableArgs.builder()
            .vpcId(my_vpc.id))
            .routes(
                RouteTableRouteArgs.builder()
                    .cidrBlock("0.0.0.0/0")
                    .gatewayId(igw.id)
                    .build());

 // Create Private route table with attaching pirvate route in to it
Routetable private_route_table = new RouteTable("privateroutetable", RouteTableArgs.builder()
.vpcId(my_vpc.id))
.route(
    RouteTableRouteArgs.builder()
    .cidrBlock("0.0.0.0/0")
    .gatewayId(igw.id)
    .build());

// Create Security group
Securitygroup secuirty_group = new SecurityGroupRule("sg", SecurityGroupRuleArgs.builder()
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
EBSvolume new_volume = new Volume("newvolume", VolumeArgs.builder()
.availabilityZone("us-east-1a")
.size(8)
.tags(Map.of("Name", "HelloWorld"))
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
Template new_template = new LaunchTemplate("newtemplate", LaunchTemplateArgs.builder()
imageId("ami-08a52ddb321b32a8c")
.instanceType("t2.micro")
.keyName("test")
.availabilityZone("us-west-2a")
.vpcSecurityGroupIds("sg.id")
.associatePublicIpAddress(true)
.disableApiTermination(true)
.build());

// Create LoadBalancer Target group
Targetgroup test_target = new TargetGroup("testtarget", TargetGroupArgs.builder()
.port(80)
.protocol("HTTP")
.vpcId(main.id())
.build());

 // Create an Application LoadBalancer
 Loadbalancer application_balancer = new LoadBalancer("applicationbalancer", LoadBalancerArgs.builder()
.internal(true)
.LoadBalancertype("application")
.SecurityGroup(igw.id)
.Subnet(private_subnet.id,private_subnet1.id));

 // Create an LoadBalancer Lister for Application LoadBalancer
Listener Application_Listener = new Listener("applicationListener", ListenerArgs.builder()
.LoadBalancerArn(applicationbalancer)
.port(80)
.protocol("HTTP")
.defaultActions(ListenerDefaultActionArgs.builder()
type("forward")
            .targetGroupArn(testtarget.arn())
            .build())
            .build());

 // Create an Autosaclling group
Autoscalling Autosaclling_group = new Group("autoscallinggroup", GroupArgs.builder()
.availabilityZones("us-east-1a")
.desiredCapacity(1)
.maxSize(1)
.minSize(1)
.launchTemplate(GroupLaunchTemplateArgs.builder()
    .id(newtemplate.id)
    .version("$Latest")
    .build())
.build());
}