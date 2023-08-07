package main

import (
	"github.com/TommyLemon/unitauto-go/unitauto"
)

func main() {
	// TODO 改成你项目的 module 路径  unitauto.DEFAULT_MODULE_PATH = "github.com/TommyLemon/unitauto-go"

	unitauto.Test()
	unitauto.Start(8082)
}
