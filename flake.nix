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
    overlays.default = final: prev: {};
    devShells = forEachSupportedSystem ({pkgs}: {
      default = pkgs.mkShell {
        packages = with pkgs; [
          babashka # Clojure interpreter for scripting
          graphviz
          neo-cowsay # just for fun
          sqlite
          temurin-bin # Eclipse Temurin, prebuilt OpenJDK binary
        ];
        shellHook = ''
          cowthink "Welcome to this nix dev shell!" --bold -f tux --rainbow
          echo "Versions"
          bb --version
          dot --version # one of the tools installed with graphviz
          java --version

          export NOT_FOUND_PAGE_REDIRECT_URI="http://localhost:$PORT/";
        '';
        JDBC_DATABASE_URL = "jdbc:sqlite:tweedler_dev.db";
        JVM_OPTS = "-Dclojure.main.report=stderr";
        PORT = 3000;
        REDIS_HOST = "127.0.0.1";
        REDIS_PORT = 6379;
      };
    });
  };
}
