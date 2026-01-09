# AnarchyCore-NextGen

## 感谢
[CrystalKillListener和它的作者GuangChen2333](https://github.com/GuangChen2333/CrystalKillListener/tree/master)

## 权限
- `anarchy.dupe.command`
- `anarchy.dupe.mine-and-place`
- `anarchy.dupe.item-frame`
- `anarchy.dupe.donkey.xin`
- `anarchy.dupe.donkey.org`
- `anarchy.dupe.chicken.xin`
- `anarchy.dupe.chicken.click`

## 构建发行版本

发行版本用于正常使用, 不含 TabooLib 本体。

```
./gradlew build
```

## 构建开发版本

开发版本包含 TabooLib 本体, 用于开发者使用, 但不可运行。

```
./gradlew taboolibBuildApi -PDeleteCode
```

> 参数 -PDeleteCode 表示移除所有逻辑代码以减少体积。