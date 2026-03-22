#!/bin/bash

SWAP_SIZE="${1:-2G}"

if [ -f /swapfile ]; then
    echo "Swap file already exists, skipping."
    exit 0
fi

echo "=== Setting up ${SWAP_SIZE} swap ==="
sudo fallocate -l $SWAP_SIZE /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
echo "Swap configured successfully."
