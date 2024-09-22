{
  description = "Tweedler. A simple app for practicing Clojure.";

  inputs = {
    alejandra = {
      url = "github:kamadorueda/alejandra/3.0.0";
    };
    nixpkgs.url = "https://flakehub.com/f/NixOS/nixpkgs/0.1.*.tar.gz";
  };

  outputs = {
    nixpkgs,
    self,
    ...
  }: let
    supportedSystems = ["x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin"];
    forEachSupportedSystem = f:
      nixpkgs.lib.genAttrs supportedSystems (system:
        f {
          pkgs = import nixpkgs {
            inherit system;
            overlays = [self.overlays.default];
          };
        });
  in {
    devShells = forEachSupportedSystem ({pkgs}: {
      default = pkgs.mkShell {
        packages = with pkgs; [
          babashka # Clojure interpreter for scripting
          neo-cowsay # just for fun
          temurin-bin # Eclipse Temurin, prebuilt OpenJDK binary
        ];
        shellHook = ''
          cowthink "Welcome to this nix dev shell!" --bold -f tux --rainbow
          echo "Versions"
          bb --version

          # export FOO=bar;
        '';
        # see gcp:zone in Pulumi.dev.yaml
        FOO = "bar";
      };
    });
  };
}
