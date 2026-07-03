#!/bin/bash
# user-data para EC2 Amazon Linux 2023: instala Docker + compose plugin + swap.
# Se ejecuta una sola vez al primer arranque de la instancia.
set -euxo pipefail

# 2 GB de swap: evita OOM al compilar Maven + npm en t3.medium (4 GB).
if [ ! -f /swapfile ]; then
  dd if=/dev/zero of=/swapfile bs=1M count=2048
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

# Docker
dnf update -y
dnf install -y docker git
systemctl enable --now docker
usermod -aG docker ec2-user

# Plugin docker compose v2
mkdir -p /usr/local/lib/docker/cli-plugins
ARCH="$(uname -m)"
curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

echo "user-data OK" > /var/log/nma-user-data.done
