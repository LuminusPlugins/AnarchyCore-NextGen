# AnarchyCore-NextGen

## 功能
所有功能都可开关。

### Duplications
- [x] Chicken Dupe (with periods setting)
- [x] Command Dupe
- [x] Donkey Dupe
- [x] Item Frame Dupe
- [x] Mine and Place Dupe (x+1)

### Miscellaneous
- [x] Player Statistics (Kills, Deaths, Joins, Quits and permission placeholders)
- [x] Suicide Command (Jump to Vanilla command if the sender is op)

### Limits
- [x] Crystal Speed Limit
- [ ] End Portal TNT Limit

## 感谢
[CrystalKillListener和它的作者GuangChen2333](https://github.com/GuangChen2333/CrystalKillListener/tree/master)

## 权限
- `anarchy.reload`
- `anarchy.suicide`
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