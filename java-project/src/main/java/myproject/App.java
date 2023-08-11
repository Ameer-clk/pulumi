package myproject;

import com.pulumi.*;
import com.pulumi.pulumi.MapOutput;
import com.pulumi.pulumi.Output;
import com.pulumi.pulumi.Stack;
import com.pulumi.pulumi.MapOutput;
import com.pulumi.pulumi.Output;
import com.pulumi.aws.ec2.*;
import com.pulumi.aws.s3.*;
import com.pulumi.aws.cloudfront.*;
import com.pulumi.aws.acm.*;
import java.util.Arrays;

public class MyStack extends Stack {
    public MyStack() {
        String accessKey = System.getenv("AKIAVH34WU7JQMX7CZ6F");
        String secretKey = System.getenv("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");

        // Create a provider with the provided access key and secret key
        Provider awsProvider = new Provider("aws", new ProviderArgs.Builder()
            .setAccessKey(accessKey)
            .setSecretKey(secretKey)
            .setRegion("us-east-1") // Update to your desired region
            .build());


        // Create VPC
        Vpc vpc = new Vpc("myVpc", new VpcArgs.Builder()
            .setCidrBlock("10.0.0.0/16")
            .build());

        // Create a Subnet
        Subnet subnet = new Subnet("publicsubnet", new SubnetArgs.Builder()
            .setVpcId(vpc.id())
            .setCidrBlock("10.1.10.0/24")
            .build());

        // Create a Subnet
        Subnet privatesubnet = new Subnet("privatesubnet", new SubnetArgs.Builder()
            .setVpcId(vpc.id())
            .setCidrBlock("10.1.20.0/24")
            .build());

        // Create an Internet Gateway
        InternetGateway igw = new InternetGateway("myIgw", new InternetGatewayArgs.Builder()
            .setVpcId(vpc.id())
            .build());

        // Create a RouteTable
        RouteTable rt = new RouteTable("publicroutetable", new RouteTableArgs.Builder()
            .setVpcId(vpc.id())
            .setRoutes(Arrays.asList(new RouteTableRouteArgs.Builder()
                .setCidrBlock("0.0.0.0/0")
                .setGatewayId(igw.id())
                .build()))
            .build());

        // Create a RouteTable
        RouteTable privatert = new RouteTable("privateroutetable", new RouteTableArgs.Builder()
            .setVpcId(vpc.id())
            .setRoutes(Arrays.asList(new RouteTableRouteArgs.Builder()
                .setCidrBlock("0.0.0.0/0")
                .setGatewayId(igw.id())
                .build()))
            .build());

        // Create a Security Group
        SecurityGroup sg = new SecurityGroup("mySg", new SecurityGroupArgs.Builder()
            .setVpcId(vpc.id())
            .ingress(Arrays.asList(new SecurityGroupIngressArgs.Builder()
                .protocol("tcp")
                .fromPort(22)
                .toPort(22)
                .cidrBlocks(Arrays.asList("0.0.0.0/0"))
                .build()))
            .egress(Arrays.asList(new SecurityGroupEgressArgs.Builder()
                .protocol("-1")
                .fromPort(0)
                .toPort(0)
                .cidrBlocks(Arrays.asList("0.0.0.0/0"))
                .build()))
            .build());

        // ... rest of your code ...

        // Exporting outputs
        this.addOutputs(
            MapOutput.of("vpcId", vpc.id()),
            MapOutput.of("subnetId", subnet.id()),
            MapOutput.of("privatesubnetId", privatesubnet.id()),
            MapOutput.of("igwId", igw.id()),
            MapOutput.of("routeTableId", rt.id()),
            MapOutput.of("privaterouteTableId", privatert.id()),
            // ... add more outputs here ...
        );
    }

    public static void main(final String[] args) {
        Pulumi.main(MyStack::new);
    }
}

