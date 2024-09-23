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
          flyctl # Command line tools for fly.io services
          graphviz
          neo-cowsay # just for fun
          sqlite
          temurin-bin # Eclipse Temurin, prebuilt OpenJDK binary
          turso-cli # CLI for Turso (distributed SQLite).
        ];
        shellHook = ''
          cowthink "Welcome to this nix dev shell!" --bold -f tux --rainbow
          echo "Versions"
          bb --version
          dot --version # one of the tools installed with graphviz
          fly version
          java --version
          turso --version

          export FLY_DEPLOY_TOKEN=$(cat /run/secrets/fly/tokens | jq .deploy | tr -d '"');
          export NOT_FOUND_PAGE_REDIRECT_URI="http://localhost:$PORT/";
          export TURSO_AUTH_TOKEN=$(cat /run/secrets/turso/tweedler | jq .auth_token | tr -d '"');
          # turso db show tweedler --http-url
          export TURSO_DATABASE_URL=$(cat /run/secrets/turso/tweedler | jq .database_url | tr -d '"');
        '';
        JDBC_DATABASE_URL = "jdbc:sqlite:tweedler_dev.db";
        # JDBC_DATABASE_URL = "jdbc:sqlite::memory:";
        JVM_OPTS = "-Dclojure.main.report=stderr";
        PORT = 3000;
        REDIS_HOST = "127.0.0.1";
        REDIS_PORT = 6379;
        # https://github.com/taoensso/timbre/wiki/1-Getting-started#configuration
        TAOENSSO_TIMBRE_MIN_LEVEL_EDN = ":debug";
        # TAOENSSO_TIMBRE_MIN_LEVEL_EDN = ":warn";
      };
    });
  };
}
