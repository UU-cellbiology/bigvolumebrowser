package bvb.io.dto;

import java.util.ArrayList;
import java.util.List;

public class ObjectStateDTO
{
    public String objectId;
    public List<PropertyStateDTO> properties = new ArrayList<>();

    public ObjectStateDTO() {}
}
