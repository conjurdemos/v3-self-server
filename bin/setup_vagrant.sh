!/bin/bash

main () {
    update_yumaptbrew
    install_vagrant
    echo "All ready for \`vagrant up\` command!"
}

update_yumaptbrew () {
    # Check if brew, yum or apt is installed; Update whichever is
    if [ "$(command -v yum)" ]; then
        sudo yum update -y
        OS_PLATFORM="el"
    elif [ "$(command -v apt)" ]; then
        sudo apt-get update
        OS_PLATFORM="deb"
    elif [ "$(command -v brew)" ]; then
        brew upgrade
        OS_PLATFORM="darwin"
    else
        RED='\033[0;31m'
        NC='\033[0m' # No Color
        echo -e "${RED}Package Manager brew, yum or apt not found. Please contribute to https://github.com/infamousjoeg/v3-self-server for your distribution of choice.${NC}"
    fi
}

install_vagrant () {
    # Check if Vagrant is installed
    if [ -z "$(command -v vagrant)" ]; then
        # Install Vagrant
        case "$OS_PLATFORM" in
            el)
                sudo yum install -y yum-utils
                sudo yum-config-manager --add-repo https://rpm.releases.hashicorp.com/RHEL/hashicorp.repo
                sudo yum -y install vagrant
                ;;

            deb)
                curl -fsSL https://apt.releases.hashicorp.com/gpg | sudo apt-key add -
                sudo apt-add-repository "deb [arch=amd64] https://apt.releases.hashicorp.com $(lsb_release -cs) main"
                sudo apt-get update && sudo apt-get install -y vagrant
                ;;

            darwin)
                brew tap hashicorp/tap
                brew install vagrant
                ;;
            *)
                echo "An error occurred."
                exit
                ;;
        esac
    fi
}
