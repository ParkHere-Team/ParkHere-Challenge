provider "aws" {
  region = "us-east-1"
}

# ----------------- Networking (VPC & Subnets) -----------------
resource "aws_vpc" "main_vpc" {
  cidr_block = "10.0.0.0/16"
}

resource "aws_subnet" "public_subnets" {
  count             = 2
  vpc_id           = aws_vpc.main_vpc.id
  cidr_block       = "10.0.${count.index}.0/24"
  map_public_ip_on_launch = true
}

resource "aws_subnet" "private_subnets" {
  count             = 2
  vpc_id           = aws_vpc.main_vpc.id
  cidr_block       = "10.0.${count.index + 2}.0/24"
}

resource "aws_internet_gateway" "gw" {
  vpc_id = aws_vpc.main_vpc.id
}

resource "aws_route_table" "public_rt" {
  vpc_id = aws_vpc.main_vpc.id
}

resource "aws_route" "public_route" {
  route_table_id = aws_route_table.public_rt.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id = aws_internet_gateway.gw.id
}

# ----------------- Security Groups -----------------
resource "aws_security_group" "lb_sg" {
  vpc_id = aws_vpc.main_vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs_sg" {
  vpc_id = aws_vpc.main_vpc.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    security_groups = [aws_security_group.lb_sg.id]
  }
}

resource "aws_security_group" "db_sg" {
  vpc_id = aws_vpc.main_vpc.id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    security_groups = [aws_security_group.ecs_sg.id]
  }
}

# ----------------- IAM Roles -----------------
resource "aws_iam_role" "ecs_execution_role" {
  name = "ecs_execution_role"
  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": { "Service": "ecs-tasks.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "ecs_execution_policy" {
  role       = aws_iam_role.ecs_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ----------------- ECS Autoscaling -----------------
resource "aws_appautoscaling_target" "ecs_target" {
  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.ecs_cluster.name}/${aws_ecs_service.ecs_service.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = 2
  max_capacity       = 5
}

resource "aws_appautoscaling_policy" "ecs_policy" {
  name               = "ecs-scaling-policy"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs_target.resource_id
  scalable_dimension = aws_appautoscaling_target.ecs_target.scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs_target.service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = 50.0
    scale_in_cooldown  = 60
    scale_out_cooldown = 60

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}

# ----------------- RDS Autoscaling -----------------
resource "aws_rds_cluster" "db_cluster" {
  cluster_identifier    = "reservation-db-cluster"
  engine                = "aurora-postgresql"
  master_username       = "<username>"
  master_password       = "<password>"
}

resource "aws_rds_cluster_instance" "db_instance" {
  count                 = 2
  identifier            = "reservation-db-instance-${count.index}"
  cluster_identifier    = aws_rds_cluster.db_cluster.id
  instance_class        = "db.t3.medium"
}

# ----------------- ECS Service -----------------
resource "aws_ecs_service" "ecs_service" {
  name            = "reservation-service"
  cluster         = aws_ecs_cluster.ecs_cluster.id
  task_definition = aws_ecs_task_definition.ecs_task.arn
  desired_count   = 2
  launch_type     = "FARGATE"
  network_configuration {
    subnets         = aws_subnet.private_subnets[*].id
    security_groups = [aws_security_group.ecs_sg.id]
  }
}

# ----------------- CloudWatch Logs -----------------
resource "aws_cloudwatch_log_group" "ecs_logs" {
  name = "/ecs/reservation-service"
}

# ----------------- S3 with Cross-Region Replication -----------------
resource "aws_s3_bucket" "primary_s3" {
  bucket = "reservation-primary-bucket"
}

resource "aws_s3_bucket_replication_configuration" "replication" {
  role = aws_iam_role.s3_replication_role.arn
  bucket = aws_s3_bucket.primary_s3.id

  rule {
    id     = "replication-rule"
    status = "Enabled"

    destination {
      bucket = aws_s3_bucket.replica_s3.arn
      storage_class = "STANDARD"
    }
  }
}

resource "aws_s3_bucket" "replica_s3" {
  bucket = "reservation-replica-bucket"
}
