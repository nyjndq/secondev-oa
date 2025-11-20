# e-code 本地二开项目
## 项目搭建
本项目为本地开发模版，可在 IDEA 中直接打开。

## 调试

本地调试：[参考文档](https://www.e-cology.com.cn/ecode/playground/doc/share/view/916733818655342593#3.4.1%20%E6%9C%AC%E5%9C%B0%E4%BB%A3%E7%A0%81%E8%B0%83%E8%AF%95)

项目使用 gradle 进行构建，在项目根目录下直接运行 gradle build 即可

## 打包部署

### 多模块项目创建说明
> 子项目会集成根目录下的关联依赖，可在build.gradle中进行修改

#### 创建子模块


> 在根目录下直接创建目录，并在目录下创建对应的gradle文件

例如：创建模块secondev-workflow-demo

1.创建目录 secondev-md-demo

2.创建gradle文件 secondev-md-demo/secondev-md-demo.gradle

```
root
  |-secondev-custom-demo
  | |-src
  | | |--...
  | |-secondev-custom-demo.gradle
  | |

```
secondev-custom-demo.gradle 内容如下 
```
description = "子模块demo项目"

dependencies {
  // 子项目私有依赖添加
}
```
#### 接触子模块关联
> 在需要接触关联的子模块目录下 添加名为.disabled的文件即可，内容为空即可 需要重新reload