import * as pulumi from "@pulumi/pulumi";
import * as aws from "@pulumi/aws";


// Create a Networking

  // Services used

  // Vpc,Subnet,RouteTable,Internet gateway,Nat gateway,Security Group

// Create a Vpc
const Vpc = new aws.ec2.Vpc("myvpc",{
    cidrBlock: "192.168.0.0/16"});

// Create a Public Subnet
const Publicsubnet = new aws.ec2.Subnet("publicsubnet", {
    vpcId: aws.vpc.myvpc.id,
    AvailabilityZone: "us-east-1a",
    cidrBlock: "192.168.10.0/24",});

// Create a Private subnet
const Privatesubnet = new aws.ec2.Subnet("privatesubnet", {
    vpcId: aws.vpc.myvpc.id,
    AvailabilityZone: "us-east-1b",
    cidrBlock: "192.168.20.0/24",});

// Create an Internetgateway
const Internetgateway = new aws.ec2.InternetGateway("igw", {
    vpcId: aws.vpc.myvpc.id,});

// Create an Elastic IP
const elasticIp = new aws.ec2.Eip("elasticip", {
    vpc: true,
});

// Cretae an Natgateway
const Natgateway = new aws.ec2.NatGateway("Publicnat", {
    allocationId: aws.eip.elastic.id,
    subnetId: aws.subnet.publicsubnet.id,});

// Create  Public Route table
const publicRouteTable = new aws.ec2.RouteTable("publicroutetable", {
    vpcId: aws.vpc.myvpc.id,
});

// Create Private Route table
const privateRouteTable = new aws.ec2.RouteTable("privateroutetbale", {
    vpcId: aws.vpc.myvpc.id,});

// Create a Public Route table association
const publicRouteTableAssociation = new aws.ec2.RouteTableAssociation("publicassociation", {
    subnetId: aws.subnet.publicsubnet.id,
    routeTableId: publicRouteTable.id,});

// Create a Private Route table association
const privateRouetTableAssociation = new aws.RoutetableAssociation("privateassociation", {
    SubnetId: aws.subnet.privatesubnet.id,
    RoutetableId: aws.routetable.privateroutetbale.id});

// Create a Public route
const publicRoute = new aws.Route("publicroute", {
    routeTableId: aws.routetable.publicroutetable.id,
    destinationIpv6CidrBlock: "0.0.0.0/0",
    gatewayId: aws.internetgateway.igw.id,});

// Create a private route
const pirvateroute = new aws.Route("privateroute", {
    routetableId: aws.routetable.privateroutetbale.id,
    destinationIpv6CidrBlock: "0.0.0.0/0",
    gatewayId: aws.internetgateway.igw.id,});

// Create a  Public Security group
const publicsecuirtygroup = new aws.ec2.SecurityGroup("publicsg", {
    description: "Allow Traffic for public subnet",
    vpcId: aws.vpc.myvpc.id,
    ingress: [{
        description: "TLS from VPC",
        fromPort: 22,
        toPort: 22,
        protocol: "tcp",
        cidrBlocks: ["0.0.0.0/0"],
    }],
    egress: [{
        fromPort: 0,
        toPort: 0,
        protocol: "-1",
        cidrBlocks: ["0.0.0.0/0"],
        ipv6CidrBlocks: ["::/0"],
    }],
    tags: {
        Name: "publicsg",
    },
});

// Create a Private Security group
const privatesecuritygroup = new aws.ec2.SecurityGroup("privatesg", {
    description: "Allow Traffic for private subnet",
    vpcId: aws.vpc.myvpc.id,
    ingress: [{
        description: "TLS from VPC",
        fromPort: 22,
        toPort: 22,
        protocol: "tcp",
        cidrBlocks: ["0.0.0.0/0"],
    }],
    egress: [{
        fromPort: 0,
        toPort: 0,
        protocol: "-1",
        cidrBlocks: ["0.0.0.0/0"],
        ipv6CidrBlocks: ["::/0"],
    }],
    tags: {
        Name: "publicsg",
    },
});

// Create an Public Instance and also creating EBS Volume
const publicweb = new aws.ec2.Instance("publicweb", {
    ami: "ami-051f7e7f6c2f40dc1",
    instanceType: "t2.micro",
    keyName: "minikube",
    vpcSecurityGroupIds: [aws.security.publicsg.id],
    subnetId: aws.subnet.publicsubnet.id,
    availabilityZone: "us-east-1a",
    privateIp: "192.168.10.1/24",
});

const publicVolume = new aws.ebs.Volume("publicvolume", { // Create an EBS Volume
    availabilityZone: "us-east-1a",
    size: 8,});

const publicSnapshot = new aws.ebs.Snapshot("publicSnapshot", { // Create an EBS Snapshot
    volumeId: aws.volume.publicvolume.id});

const publicebsAtt = new aws.ec2.VolumeAttachment("publicebsAtt", {
    deviceName: "/dev/sdh",
    instanceId: publicweb.id, // this instanceId is the id of above created EC2 instance
    volumeId: publicVolume.id,});

// Create a Private Instance and aslo creatig EBS Volume
const privateweb = new aws.ec2.Instance("privateweb", {
    ami: "ami-051f7e7f6c2f40dc1",
    instanceType: "t2.micro",
    keyName: "minikube",
    vpcSecurityGroupIds: [aws.security.privatesg.id],
    subnetId: aws.subnet.privatesubnet.id,
    availabilityZone: "us-east-1b",
    privateIp: "192.168.20.2/24",
});

const privatevolume = new aws.ebs.Volume("privatevolume", { // Create an EBS Volume
    availabilityZone: "us-east-1b",
    size: 8,});

const privateSnapshot = new aws.ebs.Snapshot("privateSnapshot", { // Create an EBS Snapshot
    volumeId: aws.volume.privatevolume.id});

const ebsAtt = new aws.ec2.VolumeAttachment("ebsAtt", { // Attaching the EBS Volume to the Private instance
    deviceName: "/dev/sdh",
    instanceId: privateweb.id, // this instanceId is the id of above created EC2 instance
    volumeId: privatevolume.id,});

// Create a Cloud watch monitoring for Private subnet instance
const privateInstance = new aws.cloudwatch.InstanceMetric("privateinstance", {
    instanceId: privateweb.id,
    metricName: "CPUUtilization",
    namespace: "AWS/EC2",
    dimensions: {
        InstanceId: privateweb.id,
    },
    unit: "Percent",
});

// Optionally, you can create an alarm based on this metric
const privatealarm= new aws.cloudwatch.MetricAlarm("privatealarm", {
    alarmName: "CPUAlarm",
    comparisonOperator: "GreaterThanOrEqualToThreshold",
    evaluationPeriods: 2,
    metricName: "CPUUtilization", // Change this to the desired metric
    namespace: "AWS/EC2",
    period: 300, // in seconds
    statistic: "Average",
    threshold: 70, // Example threshold value
    alarmActions: "privateinstancesns.arn", // Add your SNS topic ARN or other actions
    dimensions: {
         InstanceId: privateweb.id,
    },
});

// Create a Target Group for Application LoadBalancer
const test = new aws.lb.TargetGroup("test", {
    port: 80,
    protocol: "HTTP",
    vpcId: aws.vpc.myvpc.id,});

// Create a Application LoadBalancer for the Private Instance
const LoadBalancer = new aws.lb.LoadBalancer("application", {
    internal: false,
    loadBalancerType: "application",
    securityGroups: [aws.security.privatesg.id],
    subnets: [aws.subnet.privatesubnet],
    enableDeletionProtection: true,});

// Create a Application LoadBalancer Listner for the Private Instance
const applicationListener = new aws.lb.Listener("applicationListener", {
    loadBalancerArn: "application.arn",
    port: 80,
    protocol: "HTTP",
    defaultActions: [{
        type: "redirect",
        redirect: {
            port: "443",
            protocol: "HTTPS",
            statusCode: "HTTP_301",
        },
    }],
});

// Create an Launchconfiguration for Private Instance
const privateConfiguration = new aws.ec2.LaunchConfiguration("privateconfiguration", {
    // Note that we are using a specific AMI ID
    instanceId: privateweb.id,
    instanceType: "t2.micro",
    associatePublicIpAddress: true,
    securityGroups: [aws.security.privatesg.id],
    keyName: "minikube",
});

// Create an autoscaling group For Private Instance
const privateautoscalingGroup = new aws.autoscaling.Group("privateautoscalling", {
    availabilityZones: ["us-east-1a"],  // Availability zone should be the same as EC2 instance
    desiredCapacity: 1,  // Adjust as necessary
    minSize: 1,  // Adjust as necessary
    maxSize: 2,  // Adjust as necessary
    launchConfiguration: privateConfiguration.id,
    vpcZoneIdentifiers: [aws.subnet.privatesubnet.id],  // Ensure this Subnet ID is correct
    forceDelete: true,
    healthCheckType: "EC2",
});

// Create an SNS Notification for Pirvate instance
const privateInstancesns = new aws.sns.Topic("privateinstancesns", {deliveryPolicy: `{
    "http": {
      "defaultHealthyRetryPolicy": {
        "minDelayTarget": 5,
        "maxDelayTarget": 2,
        "numRetries": 3,
        "numMaxDelayRetries": 0,
        "numNoDelayRetries": 0,
        "numMinDelayRetries": 0,
        "backoffFunction": "linear"
      },
      "disableSubscriptionOverrides": false,
      "defaultThrottlePolicy": {
        "maxReceivesPerSecond": 1
      }
    }
  }
  `});

// Create a SNS Topic subscription
const topicSubscription = new aws.sns.TopicSubscription("exampleTopicSubscription", { // This will give an notification when the cpu utilization becomes heigher.
    endpoint: "test@gmail.com",
    protocol: "email",
    topic: privateInstancesns.arn,});

