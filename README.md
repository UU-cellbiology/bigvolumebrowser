[![](https://github.com/UU-cellbiology/bigvolumebrowser/actions/workflows/build.yml/badge.svg)](https://github.com/UU-cellbiology/bigvolumebrowser/actions/workflows/build.yml) 
[![Maven Scijava Version](https://img.shields.io/github/v/tag/UU-cellbiology/bigvolumebrowser?label=[Maven%20Scijava])](https://maven.scijava.org/#browse/browse:releases:nl%2Fuu%2Fscience%2Fcellbiology%2Fbigvolumebrowser)


<picture>
	<source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/UU-cellbiology/bigvolumebrowser/main/logo/bvb_logo_dark_200.png">
	<img align="right" style="padding:10px" alt="BVB logo" src="https://raw.githubusercontent.com/UU-cellbiology/bigvolumebrowser/main/logo/bvb_logo_bright_200.png">
</picture>

[FIJI](https://fiji.sc/) plugin for interactive 3D exploration of multiple large (and small) volumetric datasets and geometric shapes, built on [BigVolumeViewer](https://forum.image.sc/t/bigvolumeviewer-tech-demo/12104) ([fork](https://github.com/UU-cellbiology/bvv-playground)).  

It can display volumetric (microscopy) data, SMLM datasets, and geometric objects (point clouds and meshes) in various rendering modes.   
Objects can be clipped and transformed freely in 3D, and it works with timelapse data.

BVB performs lazy loading and supports a multi-scale pyramidal data formats.   
This speeds up render and allows exploration of datasets larger than GPU memory.   

It supports rendering of 3D visualization movies using key frame animation interface.    

**Users**: please check full description/tutorials in the **[project's wiki](https://github.com/UU-cellbiology/bigvolumebrowser/wiki)**.   


You can also watch a short **[video tutorial](https://www.youtube.com/watch?v=c0frU3WpfwE)** (big thanks to [@jomaydc](https://forum.image.sc/u/jomaydc/summary)) (12 mins)  
or a longer [extended version](https://www.youtube.com/watch?v=Z9rbxIqZNp8) (1 hour).   

For questions, tag me (<a href="https://forum.image.sc/u/ekatrukha/summary">@ekatrukha</a>) at <a href="https://forum.image.sc/">image.sc</a> forum.   

**Developers**: check some code [examples](https://github.com/UU-cellbiology/bigvolumebrowser/tree/main/src/test/java/bvb/examples).  
Feel free to reach out, since the code description/comments are not always in the best shape. 

----------
**Questions with answers:**  

What's next here? See the development [roadmap](https://github.com/UU-cellbiology/bigvolumebrowser/wiki/Improvements).  
Want to contribute? Submit a [PR](https://en.wikipedia.org/wiki/Fork_and_pull_model). Or create an [issue](https://github.com/UU-cellbiology/bigvolumebrowser/issues).  
Wanna see live progress on development? Check [this feed](https://bsky.app/hashtag/bigvolumebrowser) or less frequent updates [thread](https://forum.image.sc/t/bigvolumebrowser-a-new-3d-multi-volume-mesh-point-cloud-smlm-data-viewer/117764).  
Not happy with it? Something is fundamentally wrong?  
Consider [alternative open-source tools for 3D viewing](https://github.com/UU-cellbiology/bigvolumebrowser/wiki/Alternative-3D-viewers).    
Like it? Spread the word and give it a star on GitHub.   

----------
**Acknowledgements**  
Powered (and made possible) by a hard work of very talented people behind:   
- [ImageJ](https://github.com/imagej) 
- [FIJI](https://fiji.sc/) 
- [imglib2](https://github.com/imglib/imglib2) 
- [BioFormats](https://github.com/ome/bioformats)
- [JglTF](https://github.com/javagl/JglTF) 
- [BigDataViewer (BDV)](https://github.com/bigdataviewer) 
- [BDV-loaders](https://github.com/BIOP/bigdataviewer-image-loaders) 
- [BigVolumeViewer](https://github.com/bigdataviewer/bigvolumeviewer-core) [(playground edition)](https://github.com/UU-cellbiology/bvv-playground). 

 this application relies on components licensed under the [LGPL-2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html)
- [laszip4j](https://github.com/mreutegg/laszip4j) v.0.20   
  
----------

Developed in [Cell Biology group](http://cellbiology.science.uu.nl) of Utrecht University.  
<a href="mailto:katpyxa@gmail.com">E-mail</a> for any questions or tag <a href="https://forum.image.sc/u/ekatrukha/summary">@ekatrukha</a> at <a href="https://forum.image.sc/">image.sc</a> forum.   
Logo design [Anna Vinokurova](https://www.behance.net/bozax)
